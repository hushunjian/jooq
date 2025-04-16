package com.hushunjian.jooq.res;

import lombok.Data;

import java.util.List;

@Data
public class LogEventRes {

    private List<LogEventDetail> query;
}
