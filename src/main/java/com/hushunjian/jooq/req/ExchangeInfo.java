package com.hushunjian.jooq.req;

import lombok.Data;

import java.util.Map;

@Data
public class ExchangeInfo {

    private String name;

    private String vhost;

    private String type;

    private Boolean durable;

    private Boolean auto_delete;

    private Boolean internal;

    private Map<String, Object> arguments;
}
