package com.tz.mybatis.sample.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tz.mybatis.sample.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
