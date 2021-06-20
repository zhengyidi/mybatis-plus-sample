package com.tz.mybatis.sample;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.tz.mybatis.sample.entity.User;
import com.tz.mybatis.sample.mapper.UserMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
class SaveAndUpdateMapperTest {

    @Autowired
    private UserMapper userMapper;

    private final static long SPECIAL_ID = 999L;
    private final static String NAME = "user-" + SPECIAL_ID;

    @Test
    @Order(1)
    void save() {
        int insertSize = userMapper.insert(createUser(SPECIAL_ID));
        assertEquals(1, insertSize);
    }

    @Test
    @Order(2)
    void update() {
        User user = userMapper.selectById(SPECIAL_ID);
        if (user == null) {
            save();
            user = userMapper.selectById(SPECIAL_ID);
        }

        user.setCompanyId(SPECIAL_ID);
        int updateSize = userMapper.updateById(user);
        assertEquals(1, updateSize);

        // 尝试更新空属性
        user.setCompanyId(null);
        updateSize = userMapper.updateById(user);
        assertEquals(1, updateSize);

        // 查询结果发现会忽略空属性
        user = userMapper.selectById(SPECIAL_ID);
        assertEquals(SPECIAL_ID, user.getCompanyId());
    }

    @Test
    void updateWithWrapper() {
        User user = userMapper.selectById(SPECIAL_ID);
        if (user == null) {
            save();
            user = userMapper.selectById(SPECIAL_ID);
        }

        // 先更新 companyId 为 SPECIAL_ID
        user.setCompanyId(SPECIAL_ID);
        int updateSize = userMapper.updateById(user);
        assertEquals(1, updateSize);

        // 刷新对象
        user = userMapper.selectById(SPECIAL_ID);
        assertEquals(SPECIAL_ID, user.getCompanyId());

        LambdaUpdateWrapper<User> wrapper = new UpdateWrapper<User>().lambda().eq(User::getId, SPECIAL_ID).set(User::getCompanyId, null);
        updateSize = userMapper.update(user, wrapper);
        assertEquals(1, updateSize);

        user = userMapper.selectById(SPECIAL_ID);
        assertNull(user.getCompanyId());
    }


    /**
     * 创建 Users
     *
     * @param size 数量
     * @return User集合
     */
    List<User> createUsers(int size) {
        assert size > 0;
        List<User> users = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            users.add(createUser());
        }
        return users;
    }

    /**
     * 创建 User
     *
     * @return User对象
     */
    User createUser() {
        return createUser(null);
    }

    /**
     * 创建指定 ID 的 User 对象
     *
     * @param id id
     * @return User对象
     */
    User createUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setName("User-" + UUID.randomUUID().toString(true));
        return user;
    }

}
