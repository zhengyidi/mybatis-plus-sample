package com.tz.mybatis.sample.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tz.mybatis.sample.entity.User;
import com.tz.mybatis.sample.mapper.UserMapper;
import com.tz.mybatis.sample.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
