package com.hushunjian.jooq.res;

import lombok.Data;

import java.util.List;

@Data
public class ApmTraceRes {

    private List<ApmTraceDetailRes> data;

    private int total;
}
