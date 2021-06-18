package com.tz.mybatis.sample.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Company extends BaseModel {

    private Long id;

    private String name;

}
