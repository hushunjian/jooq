package com.hushunjian.jooq.controller;

import com.hushunjian.jooq.req.EditUserReq;
import com.hushunjian.jooq.res.Res;
import com.hushunjian.jooq.res.User;
import com.hushunjian.jooq.service.user.UserService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RequestMapping("user")
@RestController(value = "user")
public class UserController {


    @Resource
    private UserService userService;


    @ApiOperation("查询用户信息")
    @GetMapping(value = "getById")
    @ApiImplicitParam(value = "用户主键ID", name = "id", paramType = "query")
    public Res<User> getById(@RequestParam String id) {
        return Res.success(userService.getById(id));
    }

    @ApiOperation("批量插入")
    @GetMapping(value = "batchInsert")
    @ApiImplicitParam(value = "插入数量", name = "num", paramType = "query", dataTypeClass = Integer.class)
    public Res<Integer> batchInsert(@RequestParam Integer num) {
        return Res.success(userService.batchInsert(num));
    }

    @ApiOperation("批量插入")
    @GetMapping(value = "batchInsertSelective")
    @ApiImplicitParam(value = "插入数量", name = "num", paramType = "query", dataTypeClass = Integer.class)
    public Res<Integer> batchInsertSelective(@RequestParam Integer num) {
        return Res.success(userService.batchInsertSelective(num));
    }

    @ApiOperation("查询所有")
    @GetMapping(value = "findAll")
    public Res<List<User>> findAll() {
        return Res.success(userService.findAll());
    }

    @ApiOperation("查询所有")
    @GetMapping(value = "findAllMap")
    public Res<Map<String, User>> findAllMap() {
        return Res.success(userService.findAllMap());
    }

    @ApiOperation("查询所有")
    @GetMapping(value = "findAllGroup")
    public Res<Map<String, List<User>>> findAllGroup() {
        return Res.success(userService.findAllGroup());
    }

    @ApiOperation("判断是否存在")
    @GetMapping(value = "exist")
    @ApiImplicitParam(value = "用户主键ID", name = "id", paramType = "query")
    public Res<Boolean> exist(@RequestParam String id) {
        return Res.success(userService.exist(id));
    }

    @ApiOperation("编辑用户")
    @PostMapping(value = "update")
    public Res<Boolean> update(@RequestBody EditUserReq req) {
        return Res.success(userService.update(req));
    }

    @ApiOperation("根据name模糊查询")
    @GetMapping(value = "findByNameLike")
    @ApiImplicitParam(value = "名称", name = "name", paramType = "query")
    public Res<List<User>> findByNameLike(@RequestParam String name) {
        return Res.success(userService.findByNameLike(name));
    }

}
