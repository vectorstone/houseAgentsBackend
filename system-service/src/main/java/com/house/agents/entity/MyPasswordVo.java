package com.house.agents.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="MyPassword", description="修改密码的对象")
public class MyPasswordVo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}
