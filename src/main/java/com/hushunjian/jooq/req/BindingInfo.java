package com.hushunjian.jooq.req;

import lombok.Data;

import java.util.Map;

@Data
public class BindingInfo {

    private String source;

    private String vhost;

    private String destination;

    private String destination_type;

    private String routing_key;

    private Map<String, Object> arguments;
}
