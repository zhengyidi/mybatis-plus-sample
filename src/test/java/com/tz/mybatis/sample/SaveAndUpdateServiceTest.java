package com.tz.mybatis.sample;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.tz.mybatis.sample.entity.User;
import com.tz.mybatis.sample.service.UserService;
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
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MyBatis plus 使用service保存示例
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
class SaveAndUpdateServiceTest {

    @Autowired
    private UserService userService;

    /**
     * 简单的单一实体保存
     */
    @Test
    @Order(1)
    void save() {
        boolean result = userService.save(createUser());
        assertTrue(result);
    }

    /**
     * 批量保存
     */
    @Test
    @Order(2)
    void saveBatch() {
        final int SIZE = 10;
        List<User> users = createUsers(SIZE);
        boolean result = userService.saveBatch(users);
        assertTrue(result);
    }

    /**
     * 分批次批量保存
     * 以下例子表示每保存10个就刷新一次statement
     */
    @Test
    @Order(3)
    void saveBatchWithSize() {
        final int SIZE = 20;
        List<User> users = createUsers(SIZE);
        boolean result = userService.saveBatch(users, 10);
        assertTrue(result);
    }


    private final static long SPECIAL_ID = 999L;
    private final static String NAME = "user-" + SPECIAL_ID;

    /**
     * 保存或更新
     */
    @Test
    @Order(4)
    void saveOrUpdate() {
        // 先保存一个特殊id 的User
        boolean saveOrUpdateResult = userService.saveOrUpdate(createUser(SPECIAL_ID));
        User user = userService.getById(SPECIAL_ID);
        assertNotNull(user);

        // 更新
        user.setName(NAME);
        saveOrUpdateResult = userService.saveOrUpdate(user);
        assertTrue(saveOrUpdateResult);

        // 再次获取
        user = userService.getById(SPECIAL_ID);
        assertNotNull(user);
        assertEquals(NAME, user.getName());

        /*
        打开debug级别日志，可看到一下日志
         ==>  Preparing: SELECT id,name,company_id,create_time,update_time,version,deleted FROM t_user WHERE id=? AND deleted=0
         ==> Parameters: 999(Long)
         <==      Total: 0
         ==>  Preparing: INSERT INTO t_user ( id, name, create_time, update_time, deleted ) VALUES ( ?, ?, ?, ?, ? )
         ==> Parameters: 999(Long), User-9a39a7e15d2d4a0fa9f0e48f5dc8157b(String), 2021-06-18T16:10:00.669128(LocalDateTime), 2021-06-18T16:10:00.669128(LocalDateTime), 0(Integer)
         <==    Updates: 1
         ==>  Preparing: SELECT id,name,company_id,create_time,update_time,version,deleted FROM t_user WHERE id=? AND deleted=0
         ==> Parameters: 999(Long)
         <==      Total: 1
         ==>  Preparing: SELECT id,name,company_id,create_time,update_time,version,deleted FROM t_user WHERE id=? AND deleted=0
         ==> Parameters: 999(Long)
         <==      Total: 1
         ==>  Preparing: UPDATE t_user SET name=?, create_time=?, update_time=? WHERE id=? AND deleted=0
         ==> Parameters: user-999(String), 2021-06-18T16:10:01(LocalDateTime), 2021-06-18T16:10:01(LocalDateTime), 999(Long)
         <==    Updates: 1
         ==>  Preparing: SELECT id,name,company_id,create_time,update_time,version,deleted FROM t_user WHERE id=? AND deleted=0
         ==> Parameters: 999(Long)
         <==      Total: 1
         即saveOrUpdate 会先根据id以及deleted标识 查询数据是否存在
         如不存在，则进行insert，如存在则进行update
         */
    }

    /**
     * 通过条件包装器进行判断后再进行保存或更新操作
     */
    @Test
    @Order(5)
    void saveOrUpdateWithWrapper() {
        User user = userService.getById(SPECIAL_ID);
        if (user == null) {
            user = createUser(SPECIAL_ID);
            userService.save(user);
        }
        // 前提条件
        // 将用户companyId设置为空，便于重复测试
        userService.lambdaUpdate().eq(User::getId, SPECIAL_ID).set(User::getCompanyId, null).update();
        System.out.println("-------------------------------------");
        // 判断是否执行saveOrUpdate操作的条件
        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
        final long COMPANY_ID = SPECIAL_ID + 1;
        user.setCompanyId(COMPANY_ID);


//         userUpdateWrapper.lambda().isNotNull(User::getCompanyId);
        // 注意此处：如果在 updateWrapper 中使用 set 设置属性值时，那么会先执行一遍 update 语句并返回
        // 由于在未满足条件的情况下，会执行两次 update(Wrapper) -> saveOrUpdate(Entity)
        // 此时同时开启了乐观锁，导致第一次 update 失败，而进行saveOrUpdate操作，但是同时，其内部的乐观锁计数会累加一次，所以两个update都会失败。
        // 同时注意，如果使用了updateWrapper， 会直接根据wrapper转换sql为条件，以传入的entity的非空属性值进行更新，比如下面的条件，会直接转换成如下语句
        // 如果使用了 wrapper 并调用了set方法，那么会将设置的字段值追加到更新语句的后面
        // UPDATE t_user SET name=?, company_id=?, create_time=?, update_time=?, version=?, company_id=? WHERE deleted=0 AND (company_id IS NULL AND version = ?)
        // user-999(String), 1000(Long), 2021-06-20T15:08:56(LocalDateTime), 2021-06-20T15:08:56(LocalDateTime), 10(Integer), 999(Long), 9(Integer)
        userUpdateWrapper.lambda().isNotNull(User::getCompanyId).set(User::getCompanyId, SPECIAL_ID);
        // 以上条件则表示当 companyId 为空 时，进行更新 companyId 为 SPECIAL_ID 的操作， 否则直接调用 saveOrUpdate方法根据实体进行更新
        boolean saveOrUpdateResult = userService.saveOrUpdate(user, userUpdateWrapper);
        assertFalse(saveOrUpdateResult);

        // 查询User更新结果
        user = userService.getById(SPECIAL_ID);
        assertNull(user.getCompanyId());

        // 再次执行更新
        // 注意，开启乐观锁的情况下，wrapper不可重复使用，会导致乐观锁字段累加更新失败
        saveOrUpdateResult = userService.saveOrUpdate(user, userUpdateWrapper);
        assertFalse(saveOrUpdateResult);

    }

    /**
     * 根据实体的id进行更新操作
     */
    @Test
    @Order(6)
    void updateById() {
        User user = userService.getById(SPECIAL_ID);
        assertNotNull(user);

        user.setCompanyId(SPECIAL_ID);
        boolean updateResult = userService.updateById(user);
        assertTrue(updateResult);
        user = userService.getById(SPECIAL_ID);
        assertEquals(SPECIAL_ID, user.getCompanyId());

        user.setCompanyId(null);
        updateResult = userService.updateById(user);
        assertTrue(updateResult);

        user = userService.getById(SPECIAL_ID);
        // 空字段不会被更新
        assertNotEquals(null, user.getCompanyId());
    }

    /**
     * 根据 UpdateWrapper 进行更新数据
     */
    @Test
    @Order(7)
    void updateByUpdateWrapper() {
        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
        // 建议使用lambda方式，避免列名写错
        userUpdateWrapper.lambda()
                // 上面的方式等同于 userService.lambdaUpdate()
                // 更新条件
                .eq(User::getId, SPECIAL_ID)
                .set(User::getCompanyId, null);
        boolean updateResult = userService.update(userUpdateWrapper);
        /*
        等价于
        userService.lambdaUpdate()
                .eq(User::getId, SPECIAL_ID)
                .set(User::getCompanyId, null)
                .update();
         */
        assertTrue(updateResult);

        User user = userService.getById(SPECIAL_ID);
        assertNotNull(user);
        assertNull(user.getCompanyId());
    }

    /**
     * 使用whereWrapper进行更新
     */
    @Test
    @Order(8)
    void updateByWhereWrapper() {
        User user = userService.getById(SPECIAL_ID);
        assertNotNull(user);

        Long oldCompanyId = user.getCompanyId();
        boolean userNotHasCompanyId = Objects.isNull(oldCompanyId);

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.lambda().eq(User::getId, SPECIAL_ID).isNull(User::getCompanyId);

        Long newCompanyId = RandomUtil.randomLong();
        user.setCompanyId(newCompanyId);

        boolean updateResult = userService.update(user, userQueryWrapper);
        // 更新结果与是否有companyId相关，条件是如果companyId为空，才会执行更新
        assertEquals(userNotHasCompanyId, updateResult);

        user = userService.getById(SPECIAL_ID);
        // 如果已经有值，则不会更新成功；如果没有值，则会更新成功
        assertTrue(userNotHasCompanyId ? Objects.equals(newCompanyId, user.getCompanyId()) : Objects.equals(oldCompanyId, user.getCompanyId()));

        // 尝试将companyId重新设置为null
        user.setCompanyId(null);
        userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.lambda().eq(User::getId, SPECIAL_ID).isNotNull(User::getCompanyId);
        updateResult = userService.update(user, userQueryWrapper);
        assertTrue(updateResult);

        // 同样不会更新对象中属性为值为null的字段
        user = userService.getById(SPECIAL_ID);
        assertNotNull(user.getCompanyId());
    }

    @Test
    @Order(9)
    void versioned() {
        User user = userService.getById(SPECIAL_ID);
        assertNotNull(user);

        user.setCompanyId(SPECIAL_ID);
        userService.updateById(user);

        user.setCompanyId(SPECIAL_ID + 1);
        userService.updateById(user);
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
