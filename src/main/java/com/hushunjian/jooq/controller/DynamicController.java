package com.hushunjian.jooq.controller;

import com.hushunjian.jooq.service.dynamic.DynamicService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping("dynamic")
@RestController(value = "dynamic")
public class DynamicController {


    @Resource
    private DynamicService dynamicService;

    @ApiOperation("test")
    @GetMapping(value = "test")
    public void test() {
        dynamicService.test();
    }

}
