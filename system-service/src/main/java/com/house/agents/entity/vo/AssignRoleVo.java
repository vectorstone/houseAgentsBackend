package com.house.agents.entity.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Schema(description = "分配角色,用来接收对应用户的角色列表")
@Data
public class AssignRoleVo implements Serializable {

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "角色id列表")
    private List<Long> roleIdList;
}
