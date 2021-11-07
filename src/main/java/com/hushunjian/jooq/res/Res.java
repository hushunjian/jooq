package com.hushunjian.jooq.res;

import lombok.Data;

@Data
public class Res<T> {

    private static final int DEFAULT_SUCCESS_STATUS = 200;
    private static final String DEFAULT_SUCCESS_MESSAGE = "success";

    private static final int DEFAULT_FAILURE_STATUS = 500;
    private static final String DEFAULT_FAILURE_MESSAGE = "failure";

    private int status;

    private String message;

    private T body;

    private Res(int status, String message, T body) {
        this.status = status;
        this.message = message;
        this.body = body;
    }

    private Res(int status, String message) {
        this.status = status;
        this.message = message;
    }


    public static <T> Res<T> success(T body) {
        return new Res<>(DEFAULT_SUCCESS_STATUS, DEFAULT_SUCCESS_MESSAGE, body);
    }

    public static <T> Res<T> success() {
        return new Res<>(DEFAULT_SUCCESS_STATUS, DEFAULT_SUCCESS_MESSAGE);
    }


    public static <T> Res<T> failure(T body) {
        return new Res<>(DEFAULT_FAILURE_STATUS, DEFAULT_FAILURE_MESSAGE, body);
    }

    public static <T> Res<T> failure() {
        return new Res<>(DEFAULT_FAILURE_STATUS, DEFAULT_FAILURE_MESSAGE);
    }
}
