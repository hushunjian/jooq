package com.hushunjian.jooq.res;

import lombok.Data;

@Data
public class ActionResult<T> {

    private boolean success;

    private T data;
}
