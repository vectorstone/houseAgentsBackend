package com.house.agents.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Schema(description = "分配菜单")
@Data
public class AssignMenuVo implements Serializable {

    @Schema(description = "角色id")
    private Long roleId;

    @Schema(description = "菜单id列表")
    private List<Long> menuIdList;

}