package com.hushunjian.jooq.service.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class Test2Event extends ApplicationEvent {

    private final String message;

    public Test2Event(String message) {
        super(message);
        this.message = message;
    }

}
