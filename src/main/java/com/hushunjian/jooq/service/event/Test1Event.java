package com.hushunjian.jooq.service.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class Test1Event extends ApplicationEvent {

    private final String message;

    public Test1Event(String message) {
        super(message);
        this.message = message;
    }

}
