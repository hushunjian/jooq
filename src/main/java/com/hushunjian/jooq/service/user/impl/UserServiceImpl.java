package com.hushunjian.jooq.service.user.impl;

import com.hushunjian.jooq.dao.UserDao;
import com.hushunjian.jooq.req.EditUserReq;
import com.hushunjian.jooq.res.User;
import com.hushunjian.jooq.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Resource
    private UserDao userDao;

    @Override
    public User getById(String id) {
        return userDao.getById(id);
    }

    @Override
    public int batchInsert(int num) {
        return userDao.batchInsert(num);
    }

    @Override
    public int batchInsertSelective(int num) {
        return userDao.batchInsertSelective(num);
    }

    @Override
    public List<User> findAll() {
        return userDao.findAll();
    }

    @Override
    public Map<String, User> findAllMap() {
        return userDao.findAllMap();
    }

    @Override
    public Map<String, List<User>> findAllGroup() {
        return userDao.findAllGroup();
    }

    @Override
    public boolean exist(String id) {
        return userDao.exist(id);
    }

    @Override
    public boolean update(EditUserReq req) {
        return userDao.update(req);
    }

    @Override
    public List<User> findByNameLike(String name) {
        return userDao.findByNameLike(name);
    }

    @Override
    public User getUserByView(String id) {
        return userDao.getUserByView(id);
    }
}
