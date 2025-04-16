package com.hushunjian.jooq.service.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.annotation.Resource;

@Slf4j
@Component
public class TestEventListener {

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;



    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void test1(Test1Event event) {
        System.out.println(event.getMessage());
        applicationEventPublisher.publishEvent(new Test2Event("test1里发的消息2"));
    }


    @TransactionalEventListener
    public void test2(Test2Event event) {
        System.out.println(event.getMessage());
    }
}
