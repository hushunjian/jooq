package com.hushunjian.jooq.helper;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.tools.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@Slf4j
@Component
public class RpaBotActuatorHelper implements ApplicationContextAware {

    @Resource
    private ConfigurableListableBeanFactory beanFactory;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void executeActuator(RpaBotActuator rpaBotActuator) {
        // 处理类信息
        dealWithRpaBotActuator(rpaBotActuator);
        // 类路径
        String classPath = rpaBotActuator.tempClassPath();
        log.info("类路径:[{}]", classPath);
        // 检查class是否存在
        checkClass(rpaBotActuator, classPath);
        log.info("class:[{}]方法:[{}]开始执行", rpaBotActuator.getClassName(), rpaBotActuator.getMethod());
        // 执行对应的方法
        executeMethod(getBean(rpaBotActuator.getClassName(), classPath), rpaBotActuator.getMethod());
        log.info("class:[{}]方法:[{}]结束执行", rpaBotActuator.getClassName(), rpaBotActuator.getMethod());
    }

    private Object getBean(String className, String classPath) {
        try {
            // 获取bean
            return getBean(className);
        } catch (BeansException ex) {
            // 注入bean
            registerClass(className, getBeanClass(className, classPath));
            // 重新获取
            return getBean(className);
        }
    }

    private Object getBean(String className) {
        return applicationContext.getBean(StringUtils.uncapitalize(className));
    }

    private Class<?> getBeanClass(String className, String classPath) {
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{new File(classPath).toURI().toURL()}, getClass().getClassLoader())) {
            return classLoader.loadClass(className);
        } catch (Exception e) {
            log.error("加载或注册类时出错: {}", e.getMessage(), e);
            throw new RuntimeException();
        }
    }

    private void registerClass(String className, Class<?> clazz) {
        // 注册到Spring容器
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        BeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
        // 设置当前bean定义对象是单利的
        beanDefinition.setScope("singleton");
        // 将变量首字母置小写
        ((DefaultListableBeanFactory)beanFactory).registerBeanDefinition(StringUtils.uncapitalize(className), beanDefinition);
    }

    private void executeMethod(Object beanInstance, String methodName) {
        try {
            Method method = beanInstance.getClass().getDeclaredMethod(methodName);
            method.invoke(beanInstance);
        } catch (Exception e) {
            log.error("执行方法时出错: {}", e.getMessage(), e);
        }
    }

    private void checkClass(RpaBotActuator rpaBotActuator, String classPath) {
        // 判断class文件是否存在
        File classFile = new File(String.format("%s%s%s.class", classPath, File.separator, rpaBotActuator.getClassName()));
        if (classFile.exists()) {
            // class文件存在
            log.info("文件夹[{}]下存在class文件:[{}]", classPath, rpaBotActuator.getClassName());
            return;
        }
        // 不存在就创建
        // 判断文件夹是否存在
        File classFolder = new File(classPath);
        if (!classFolder.exists() && !classFolder.mkdirs()) {
            throw new RuntimeException();
        }
        // 写入源文件
        writeClass(rpaBotActuator, classPath);
    }

    private void dealWithRpaBotActuator(RpaBotActuator rpaBotActuator) {
        String className = rpaBotActuator.getClassName();
        // 修改类名
        rpaBotActuator.setClassName(String.format("%s%s%s", rpaBotActuator.getClassName(), rpaBotActuator.getId(), rpaBotActuator.getVersion()));
        // 类内容
        String classDetailInfo = rpaBotActuator.getClassDetailInfo();
        if (StringUtils.startsWith(classDetailInfo, "package")) {
            // 去除包信息
            classDetailInfo = classDetailInfo.substring(classDetailInfo.indexOf(";"));
        }
        // 替换类名
        classDetailInfo = classDetailInfo
                .replace(String.format("public class %s", className), String.format("public class %s", rpaBotActuator.getClassName()))
                .replace(String.format("%s.class", className), String.format("%s.class", rpaBotActuator.getClassName()));
        // 设置回去
        rpaBotActuator.setClassDetailInfo(classDetailInfo);
    }

    private void writeClass(RpaBotActuator rpaBotActuator, String classPath) {
        Path dirPath = Paths.get(classPath);
        Path sourceFilePath = dirPath.resolve(String.format("%s.java", rpaBotActuator.getClassName()));
        try {
            Files.createDirectories(dirPath);
            try (BufferedWriter writer = Files.newBufferedWriter(sourceFilePath, StandardCharsets.UTF_8)) {
                writer.write(rpaBotActuator.getClassDetailInfo());
            }
            log.info("Java源文件[{}]已写入:[{}]", rpaBotActuator.getClassName(), sourceFilePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("处理文件时出错:{}", e.getMessage(), e);
            throw new RuntimeException();
        }
        // 获取系统Java编译器
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(new DiagnosticCollector<>(), null, null);
        // 将Java源文件转换为Java文件对象
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Lists.newArrayList(sourceFilePath.toFile()));
        // 设置编译选项,包括输出目录
        Iterable<String> options = Arrays.asList("-d", classPath);
        // 创建编译任务
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, options, null, compilationUnits);
        // 执行编译任务
        if (BooleanUtils.isTrue(task.call())) {
            log.info("class文件[{}]编译成功!", rpaBotActuator.getClassName());
            try {
                fileManager.close();
            } catch (IOException e) {
                log.error("关闭文件管理器时出错: {}", e.getMessage(), e);
            }
        } else {
            throw new RuntimeException();
        }
    }
}
