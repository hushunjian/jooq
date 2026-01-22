package com.hushunjian.jooq.res;

import lombok.Data;

import java.util.List;

@Data
public class ApmTraceDetailInfoRes {

    private List<ApmTraceDetailInfoSpanRes> spans;
}
