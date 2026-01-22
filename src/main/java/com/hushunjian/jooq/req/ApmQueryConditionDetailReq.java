package com.hushunjian.jooq.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApmQueryConditionDetailReq {

    private String maxTraceDuration;


    private String minTraceDuration;


    private String queryOrder;


    private String serviceId;


    private String traceState;

    private ApmPaging paging;


    private ApmQueryDurationReq queryDuration;

}
