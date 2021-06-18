package com.tz.mybatis.sample.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tz.mybatis.sample.entity.Company;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CompanyMapper extends BaseMapper<Company> {
}
