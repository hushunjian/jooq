package com.hushunjian.jooq.service.dynamic.impl;

import com.alibaba.fastjson.JSONObject;
import com.hushunjian.jooq.helper.RpaBotActuator;
import com.hushunjian.jooq.helper.RpaBotActuatorHelper;
import com.hushunjian.jooq.service.dynamic.DynamicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.tools.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;

@Slf4j
@Service
public class DynamicServiceImpl implements DynamicService {

    @Resource
    private RpaBotActuatorHelper rpaBotActuatorHelper;

    @Override
    public void test() {
        RpaBotActuator rpaBotActuator = new RpaBotActuator();
        rpaBotActuator.setId("33333333333");
        rpaBotActuator.setVersion(0);
        rpaBotActuator.setClassName("HelloWorld");
        String sourceCode = "public class HelloWorld {\n" +
                "    public static void main() {\n" +
                "        System.out.println(\"Hello, World from version: \" + 1);\n" +
                "    }\n" +
                "}\n";
        rpaBotActuator.setClassDetailInfo(sourceCode);
        rpaBotActuator.setMethod("main");
        rpaBotActuator.setMethodArguments("sss", 123, true, new Date());

        System.out.println(JSONObject.toJSONString(rpaBotActuator));
        //rpaBotActuatorHelper.executeActuator(rpaBotActuator);
    }

    public static void main(String[] args) {
        RpaBotActuator rpaBotActuator = new RpaBotActuator();
        rpaBotActuator.setId("33333333333");
        rpaBotActuator.setVersion(0);
        rpaBotActuator.setClassName("HelloWorld");
        String sourceCode = "public class HelloWorld {\n" +
                "    public static void main() {\n" +
                "        System.out.println(\"Hello, World from version: \" + 1);\n" +
                "    }\n" +
                "}\n";
        rpaBotActuator.setClassDetailInfo(sourceCode);
        rpaBotActuator.setMethod("main");
        rpaBotActuator.setMethodArguments("sss", 123, true, new Date());
        Object[] methodArguments = rpaBotActuator.getMethodArguments();
        System.out.println(JSONObject.toJSONString(rpaBotActuator));
    }

    private void test(String dd) {
        // Java 源代码文本
        String sourceCode = "public class HelloWorld {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello, World from version: \" + 1);\n" +
                "    }\n" +
                "}\n";
        int version = 1; // 假设这是当前版本号
        String className = "HelloWorld";
        String methodName = "main";
        String classJava = className + ".java";

        // 系统临时目录
        String tmp = System.getProperty("java.io.tmpdir");
        // 输出路径，包含版本号作为子目录
        String output = tmp + File.separator + className + File.separator + "v" + version;
        Path dirPath = Paths.get(output);

        Path sourceFilePath = dirPath.resolve(classJava); // 改进：使用resolve方法生成完整路径
        try {
            Files.createDirectories(dirPath);

            try (BufferedWriter writer = Files.newBufferedWriter(sourceFilePath, StandardCharsets.UTF_8)) {
                writer.write(sourceCode);
            }

            log.info("Java源文件已写入: {}", sourceFilePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("处理文件时出错: {}", e.getMessage(), e);
            return;
        }

        // 获取系统 Java 编译器
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        // 将 Java 源文件转换为 Java 文件对象
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFilePath.toFile())); // 改进：传入File对象而不是字符串列表

        // 设置编译选项，包括输出目录
        Iterable<String> options = Arrays.asList("-d", output);

        // 创建编译任务
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, options, null, compilationUnits);

        // 执行编译任务
        boolean success = task.call();

        if (success) {
            System.out.println("Compilation succeeded");

            // 加载并运行生成的类
            try {
                URLClassLoader classLoader = new URLClassLoader(new URL[]{new File(output).toURI().toURL()});
                Class<?> helloWorldClass = Class.forName(className, true, classLoader);
                helloWorldClass.getDeclaredMethod(methodName, String[].class).invoke(null, (Object) new String[0]);
            } catch (Exception e) {
                log.error("加载或执行动态编译类时发生错误: {}", e.getMessage(), e);
            }
        } else {
            log.error("Compilation failed with errors:\n{}", diagnostics.getDiagnostics());
        }

        // 关闭文件管理器
        try {
            fileManager.close();
        } catch (IOException e) {
            log.error("关闭文件管理器时出错: {}", e.getMessage(), e);
        }
    }
}