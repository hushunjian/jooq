package com.hushunjian.jooq.res;

import lombok.Data;

import java.util.List;

@Data
public class ApmTraceDetailInfoSpanRes {

    private long startTime;
    private long endTime;
    private String endpointName;
    private String layer;
    private List<ApmTraceDetailInfoSpanTagRes> tags;
}
