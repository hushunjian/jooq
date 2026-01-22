package com.hushunjian.jooq.req;

import lombok.Data;

@Data
public class SqlExInfo {

    private Integer sql_id;

    private String sql_a;

    private String sql_b;

    private Integer a_execution_ms;

    private Integer b_execution_ms;
}
