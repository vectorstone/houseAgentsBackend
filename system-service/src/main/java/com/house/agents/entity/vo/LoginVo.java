package com.house.agents.entity.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录对象
 */
@Data
@Schema(description = "登录对象")
public class LoginVo implements Serializable {
    @Schema(description = "登录用户的用户名")
    private String username;
    @Schema(description = "登录用户的密码")
    private String password;
}
