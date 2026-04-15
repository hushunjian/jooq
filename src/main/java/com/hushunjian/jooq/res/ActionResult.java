package com.hushunjian.jooq.res;

import lombok.Data;

import java.util.List;

@Data
public class ActionResult<T> {

    private boolean success;

    private List<Object> errors;

    private T data;
}
