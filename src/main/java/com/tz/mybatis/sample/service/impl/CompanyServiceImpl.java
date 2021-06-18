package com.tz.mybatis.sample.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tz.mybatis.sample.entity.Company;
import com.tz.mybatis.sample.mapper.CompanyMapper;
import com.tz.mybatis.sample.service.CompanyService;
import org.springframework.stereotype.Service;

@Service
public class CompanyServiceImpl extends ServiceImpl<CompanyMapper, Company> implements CompanyService {

}
