package com.hushunjian.jooq.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@Data
@Slf4j
public class RpaBotActuator {

    private String id;

    private Integer version;

    private String className;

    private String classDetailInfo;

    private String method;

    private String methodArgumentsJson;


    public void setMethodArguments(Object... arguments) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.methodArgumentsJson = mapper.writeValueAsString(arguments);
        } catch (JsonProcessingException e) {
            log.error("序列化方法参数时出错: {}", e.getMessage(), e);
        }
    }

    public Object[] getMethodArguments() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return Arrays.stream(mapper.readValue(methodArgumentsJson, Object[].class))
                    .toArray();
        } catch (IOException e) {
            log.error("反序列化方法参数时出错: {}", e.getMessage(), e);
            return null; // 或抛出异常
        }
    }

    public String tempClassPath() {
        // 系统路径
        String systemPath = System.getProperty("java.io.tmpdir");
        // class文件夹路径
        return String.format("%s%s%s%s%s%s",
                // 系统路径
                systemPath,
                // 类名
                this.className, File.separator,
                // id
                this.id, File.separator,
                // 版本
                this.version);
    }
}
