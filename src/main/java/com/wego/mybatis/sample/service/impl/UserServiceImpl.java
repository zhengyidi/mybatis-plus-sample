package com.wego.mybatis.sample.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wego.mybatis.sample.entity.User;
import com.wego.mybatis.sample.mapper.UserMapper;
import com.wego.mybatis.sample.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
