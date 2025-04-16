package com.hushunjian.jooq.req;

import lombok.Data;

@Data
public class QueryLogEventReq {

    private String cookie;

    private String authorization;


    private String url;

    private QueryDBReq queryDBReq;
}
