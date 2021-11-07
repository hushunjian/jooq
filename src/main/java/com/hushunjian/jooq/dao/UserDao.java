package com.hushunjian.jooq.dao;


import com.google.common.collect.Lists;
import com.hushunjian.jooq.generator.tables.UserTable;
import com.hushunjian.jooq.generator.tables.records.UserRecord;
import com.hushunjian.jooq.req.EditUserReq;
import com.hushunjian.jooq.res.User;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.impl.UpdatableRecordImpl;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Repository
public class UserDao {


    private UserTable user = UserTable.USER.as("user");


    @Resource
    private DSLContext dslContext;


    public User getById(String id) {
        log.info("getById");
        return dslContext.selectFrom(user).where(user.ID.eq(id)).fetchOneInto(User.class);
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
        }
        return dslContext.batchInsert(users).execute().length;
    }

    public List<User> findAll() {
        return dslContext.selectFrom(user).fetchInto(User.class);
    }

    public Map<String, User> findAllMap() {
        // key:id value:name
        Map<String, String> idNameMap = dslContext.selectFrom(user).fetchMap(user.ID, user.NAME);
        idNameMap.forEach((id, name) -> log.info("id:[{}], name:[{}]", id, name));

        // key:id value:age
        Map<String, Integer> idAgeMap = dslContext.selectFrom(user).fetchMap(user.ID, user.AGE);
        idAgeMap.forEach((id, age) -> log.info("id:[{}], age:[{}]", id, age));

        // key:id value:age+10
        Map<String, Integer> idAgeAddMap = dslContext.select(user.ID, user.AGE).from(user).fetchMap(user.ID, user.AGE.add(10));
        idAgeAddMap.forEach((id, age) -> log.info("id:[{}], age:[{}]", id, age));

        // key:id value:输出对象
        return dslContext.selectFrom(user).fetchMap(user.ID, User.class);
    }

    public Map<String, List<User>> findAllGroup() {
        return dslContext.selectFrom(user).fetchGroups(user.NAME, User.class);
    }

    public boolean exist(String id) {
        return dslContext.fetchExists(user, user.ID.eq(id));
    }

    public boolean update(EditUserReq req) {
        // 方式1
        dslContext.update(user).set(user.AGE, req.getAge()).set(user.NAME, req.getName()).where(user.ID.eq(req.getId())).execute();

        // 方式2
        UserRecord user = dslContext.selectFrom(this.user).where(this.user.ID.eq(req.getId())).fetchOne();
        if (user != null) {
            user.setName(req.getName());
            user.setAge(req.getAge());
            dslContext.executeUpdate(user);
        }
        return true;
    }

    public List<User> findByNameLike(String name) {
        // startWith
        dslContext.selectFrom(user).where(user.NAME.startsWith(name)).fetchInto(User.class);
        // endsWith
        dslContext.selectFrom(user).where(user.NAME.endsWith(name)).fetchInto(User.class);
        // like
        dslContext.selectFrom(user).where(user.NAME.like(name)).fetchInto(User.class);
        // contains
        return dslContext.selectFrom(user).where(user.NAME.contains(name)).fetchInto(User.class);
    }

}
