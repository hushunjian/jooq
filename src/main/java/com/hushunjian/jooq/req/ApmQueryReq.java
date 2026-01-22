package com.hushunjian.jooq.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApmQueryReq {

    private String query;

    private ApmQueryConditionReq variables;

    private String cookie;


    private String logEventCookie;


    private String logEventAuthorization;
}
