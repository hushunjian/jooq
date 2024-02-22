package com.hushunjian.jooq.helper;

import lombok.Data;

import java.io.File;

@Data
public class RpaBotActuator {

    private String id;

    private Integer version;

    private String className;

    private String classDetailInfo;

    private String method;

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
