package com.wego.mybatis.sample.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseModel {

    private Long id;

    private String name;

    @TableField("company_id")
    private Long companyId;

}
