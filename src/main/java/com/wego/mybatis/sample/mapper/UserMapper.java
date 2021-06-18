package com.wego.mybatis.sample.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wego.mybatis.sample.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
