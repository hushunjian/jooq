package com.hushunjian.jooq.dao;


import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.hushunjian.jooq.generator.tables.UserTable;
import com.hushunjian.jooq.generator.tables.records.UserRecord;
import com.hushunjian.jooq.req.EditUserReq;
import com.hushunjian.jooq.res.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.util.JsonUtils;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.jooq.Name;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.hushunjian.jooq.mapping.UserMapping.USER_MAPPING;

@Slf4j
@Repository
public class UserDao {


    private final UserTable userTable = UserTable.USER.as("user");


    @Resource
    private DSLContext dslContext;


    public User getById(String id) {
        log.info("getById");
        return dslContext.selectFrom(userTable).where(userTable.ID.eq(id)).fetchOneInto(User.class);
    }

    public int batchInsert(int num) {
        List<UserRecord> users = Lists.newArrayList();
        for (int i = 0; i < num; i++) {
            UserRecord user = new UserRecord();
            user.setId(UUID.randomUUID().toString());
            users.add(user);
        }
        return dslContext.batchInsert(users).execute().length;
    }

    public int batchInsertSelective(int num) {
        List<UserRecord> users = Lists.newArrayList();
        for (int i = 0; i < num; i++) {
            UserRecord user = new UserRecord();
            user.setId(UUID.randomUUID().toString());
            if (i % 2 == 0) {
                user.setName(i + "");
            } else {
                user.setAge(i);
            }
            users.add(user);
            // 创建视图
            dslContext.createView("user" + user.getId()).as(DSL.selectFrom(userTable).where(userTable.ID.eq(user.getId()))).execute();
        }
        return dslContext.batchInsert(users).execute().length;
    }

    public List<User> findAll() {
        return dslContext.selectFrom(userTable).fetchInto(User.class);
    }

    public Map<String, User> findAllMap() {
        // key:id value:name
        Map<String, String> idNameMap = dslContext.selectFrom(userTable).fetchMap(userTable.ID, userTable.NAME);
        idNameMap.forEach((id, name) -> log.info("id:[{}], name:[{}]", id, name));

        // key:id value:age
        Map<String, Integer> idAgeMap = dslContext.selectFrom(userTable).fetchMap(userTable.ID, userTable.AGE);
        idAgeMap.forEach((id, age) -> log.info("id:[{}], age:[{}]", id, age));

        // key:id value:age+10
        Map<String, Integer> idAgeAddMap = dslContext.select(userTable.ID, userTable.AGE.add(10)).from(userTable).fetchMap(userTable.ID, userTable.AGE.add(10));
        idAgeAddMap.forEach((id, age) -> log.info("id:[{}], age:[{}]", id, age));

        // key:id value:输出对象
        return dslContext.selectFrom(userTable).fetchMap(userTable.ID, User.class);
    }

    public Map<String, List<User>> findAllGroup() {
        return dslContext.selectFrom(userTable).fetchGroups(userTable.NAME, User.class);
    }

    public boolean exist(String id) {
        return dslContext.fetchExists(userTable, userTable.ID.eq(id));
    }

    public boolean update(EditUserReq req) {
        // 方式1
        dslContext.update(userTable).set(userTable.AGE, req.getAge()).set(userTable.NAME, req.getName()).where(userTable.ID.eq(req.getId())).execute();

        // 方式2
        UserRecord user = dslContext.selectFrom(this.userTable).where(this.userTable.ID.eq(req.getId())).fetchOne();
        if (user != null) {
            user.setName(req.getName());
            user.setAge(req.getAge());
            dslContext.executeUpdate(user);
        }
        return true;
    }

    public List<User> findByNameLike(String name) {
        // startWith
        dslContext.selectFrom(userTable).where(userTable.NAME.startsWith(name)).fetchInto(User.class);
        // endsWith
        dslContext.selectFrom(userTable).where(userTable.NAME.endsWith(name)).fetchInto(User.class);
        // like
        dslContext.selectFrom(userTable).where(userTable.NAME.like(name)).fetchInto(User.class);
        // like _
        dslContext.selectFrom(userTable).where(userTable.NAME.like(DSL.concat(name, "_"))).fetchInto(User.class);
        // like __
        dslContext.selectFrom(userTable).where(userTable.NAME.like(DSL.concat(name, "__"))).fetchInto(User.class);
        // contains
        return dslContext.selectFrom(userTable).where(userTable.NAME.contains(name)).fetchInto(User.class);
    }

    public User getUserByView(String id) {
        return dslContext.selectFrom(DSL.name("user" + id)).fetchOneInto(User.class);
    }

}
