package com.hushunjian.jooq.req;

import lombok.Data;

import java.util.Map;

@Data
public class QueueInfo {

    private String name;

    private String vhost;

    private Boolean durable;

    private Boolean auto_delete;

    private Map<String, Object> arguments;
}
