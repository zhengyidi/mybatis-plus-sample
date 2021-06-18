package com.wego.mybatis.sample.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wego.mybatis.sample.entity.Company;
import com.wego.mybatis.sample.mapper.CompanyMapper;
import com.wego.mybatis.sample.service.CompanyService;
import org.springframework.stereotype.Service;

@Service
public class CompanyServiceImpl extends ServiceImpl<CompanyMapper, Company> implements CompanyService {

}
