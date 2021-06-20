package com.tz.mybatis.sample;


import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.tz.mybatis.sample.entity.Company;
import com.tz.mybatis.sample.entity.User;
import com.tz.mybatis.sample.service.CompanyService;
import com.tz.mybatis.sample.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.runner.RunWith;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
class SelectServiceTest {

    @Autowired
    private UserService userService;
    @Autowired
    private CompanyService companyService;

    @Test
    @Order(1)
    void addData() {
        List<User> users = createUsers(30);
        boolean result = userService.saveBatch(users, 15);
        assertTrue(result);

        User user = createUser(SPECIAL_ID);
        user.setName(USER_NAME);
        user.setCompanyId(SPECIAL_ID);
        result = userService.save(user);
        assertTrue(result);

        Company company = new Company();
        company.setId(SPECIAL_ID);
        company.setName(COMPANY_NAME);
        result = companyService.save(company);
        assertTrue(result);

    }


    @Test
    @Order(2)
    void getById() {
        User user = userService.getById(SPECIAL_ID);
        assertNotNull(user);

        System.out.println(new Gson().toJson(user));
    }

    @Test
    @Order(3)
    void getOneByWrapper() {
        // 查询 companyId 不为空的
        LambdaQueryWrapper<User> queryWrapper = new QueryWrapper<User>().lambda().isNotNull(User::getCompanyId);
        User user = userService.getOne(queryWrapper);
        assertNotNull(user);

        // 查询结果不只一个的时候
        LambdaQueryWrapper<User> newWrapper = new QueryWrapper<User>().lambda().isNotNull(User::getName);
        // 通过参数设置是否抛出异常
        assertThrows(MyBatisSystemException.class, () -> userService.getOne(newWrapper));
        assertDoesNotThrow(() -> userService.getOne(newWrapper, false));

        // 只会存在返回一个结果的map映射
        Map<String, Object> map = userService.getMap(newWrapper);
        System.out.println(new Gson().toJson(map));


        // 设置查询多个字段
        newWrapper.select(User::getName, User::getId).isNotNull(User::getCompanyId);

        String userName = userService.getObj(newWrapper, objs -> (String) objs);
        // 结果是只会返回第一个设置的字段
        assertEquals(USER_NAME, userName);


        newWrapper.clear();
        // 返回多个结果时， 会重复调用很多次 转换方法
        newWrapper.select(User::getId, User::getName);
        List<Long> ids = new ArrayList<>();
        userService.getObj(newWrapper, objs -> {
            System.out.println(objs);
            ids.add((Long) objs);
            return null;
        });
        assertFalse(ids.isEmpty());
    }

    @Test
    @Order(4)
    void list() {
        List<User> list = userService.list();
        assertFalse(list.isEmpty());


        List<Object> objects = userService.listObjs();
        // 等同于 以下两种
        // 1. userService.listObjs(Function.identity());
        // 2. userService.listObjs(Wrappers.emptyWrapper(), Function.identity());
        // 仍然只会返回查询的第一个字段值
        System.out.println(new Gson().toJson(objects));

        // 以 map 形式返回对象
        List<Map<String, Object>> maps = userService.listMaps();
        System.out.println(new Gson().toJson(maps));
    }

    @Test
    @Order(5)
    void page() {
        Page<User> page = userService.page(new Page<>());
        // 默认查询10页
        List<User> records = page.getRecords();
        assertEquals(10, records.size());

        Page<User> page1 = userService.page(new Page<>(1L, 3L, true));
        List<User> records1 = page1.getRecords();
        assertEquals(3, records1.size());

    }

    @Test
    @Order(6)
    void count() {
        int count = userService.count();
        assertTrue(count > 0);

        LambdaQueryWrapper<User> queryWrapper = new QueryWrapper<User>().lambda().eq(User::getCompanyId, SPECIAL_ID);
        count = userService.count(queryWrapper);
        assertEquals(1, count);
    }


    private final static long SPECIAL_ID = 999L;
    private final static String USER_NAME = "User-" + SPECIAL_ID;
    private final static String COMPANY_NAME = "Company-" + SPECIAL_ID;

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
