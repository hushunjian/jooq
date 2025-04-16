package com.hushunjian.jooq.res;

import lombok.Data;

import java.util.Map;

@Data
public class LogEventDetail {

    private LogEventMessageRes info;

    private Map<String, String> stream;
}
