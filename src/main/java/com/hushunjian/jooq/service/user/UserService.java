package com.hushunjian.jooq.service.user;

import com.hushunjian.jooq.req.EditUserReq;
import com.hushunjian.jooq.res.User;

import java.util.List;
import java.util.Map;

public interface UserService {


    /**
     * 根据ID查找用户
     *
     * @param id ID
     * @return   用户实体
     */
    User getById(String id);

    /**
     * 根据ID通过视图查找用户
     *
     * @param id ID
     * @return   用户实体
     */
    User getUserByView(String id);


    /**
     * 批量插入
     *
     * @param num 数量
     * @return   实际入库数量
     */
    int batchInsert(int num);

    /**
     * 批量插入
     *
     * @param num 数量
     * @return   实际入库数量
     */
    int batchInsertSelective(int num);

    /**
     * 查询全部
     *
     * @return 集合
     */
    List<User> findAll();


    /**
     * 查询全部
     *
     * @return map key:id
     *             value:对象
     */
    Map<String, User> findAllMap();

    /**
     * 查询全部
     *
     * @return map
     */
    Map<String, List<User>> findAllGroup();

    /**
     * 判断是否存在
     *
     * @param id 主键ID
     * @return true:存在;false:不存在
     */
    boolean exist(String id);

    /**
     * 编辑
     *
     * @param req 编辑内容
     * @return  true:成功;false:失败
     */
    boolean update(EditUserReq req);

    /**
     * 根据名称模糊查询
     *
     * @param name 名称
     * @return    结果集
     */
    List<User> findByNameLike(String name);
}
