package com.hushunjian.jooq.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryProdScheduleTaskReq {

    private String prodCookie;

    private Integer pageNo;

    private Integer pageSize;

    private Criteria criteria;


    private String addTaskEnvCookie;

    private String downloadFileCookie;

    private String uploadFileCookie;

    @Data
    static class Criteria {
        private List<Condition> conditions;
    }

    @Data
    static class Condition {

        private String propertyName;

        private String operator;

        private String value;
    }

}

