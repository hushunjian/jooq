package com.hushunjian.jooq.res;

import lombok.Data;

import java.util.List;

@Data
public class ApmTraceDetailRes {

    private int duration;

    private List<String> endpointNames;

    private String key;

    private String start;

    private List<String> traceIds;
}
