package com.hushunjian.jooq.controller;

import com.hushunjian.jooq.service.event.EventService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping("event")
@RestController(value = "event")
public class EventController {


    @Resource
    private EventService eventService;

    @ApiOperation("test1")
    @GetMapping(value = "test1")
    public void test1() {
        eventService.test1();
    }
}
