package com.hushunjian.jooq.res;

import lombok.Data;

@Data
public class ProdScheduleTaskDetail {

    private String appId;

    private String callbackUrl;

    private String cronExpression;

    private String httpMethod;

    private String jobCode;

    private String jobName;

    private Integer periodic;

    private String protocol;
}
