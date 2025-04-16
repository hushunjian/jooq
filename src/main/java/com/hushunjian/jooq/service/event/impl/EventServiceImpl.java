package com.hushunjian.jooq.service.event.impl;

import com.hushunjian.jooq.service.event.EventService;
import com.hushunjian.jooq.service.event.Test1Event;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class EventServiceImpl implements EventService {

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void test1() {
        applicationEventPublisher.publishEvent(new Test1Event("test1的消息"));
    }
}
