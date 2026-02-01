package com.house.agents.controller;


import com.house.agents.entity.vo.AssignRoleVo;
import com.house.agents.result.R;
import com.house.agents.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.Map;

/**
 * <p>
 * 角色 前端控制器
 * </p>
 *
 * @author Gavin
 * @since 2023-08-03
 */
@RestController
@RequestMapping("/admin/sysRole")
@CrossOrigin
public class SysRoleController {
    @Resource
    SysRoleService sysRoleService;
    //查询用户当前的角色和所有的角色列表
    @PreAuthorize("hasAnyAuthority('bnt.sysUser.assignRole')")
    @Operation(summary = "加载角色列表(包括所有的角色和当前用户拥有的角色id)")
    @GetMapping("/toAssign/{userId}")
    public R getAssign(
            @Parameter(description = "用户id",required = true)
            @PathVariable String userId
    ){
        Map<String,Object> userRolesMap = sysRoleService.getRolesByUserId(userId);
        return R.ok().data(userRolesMap);
    }
    //给用户重新分配角色
    @PreAuthorize("hasAnyAuthority('bnt.sysUser.assignRole')")
    @Operation(summary = "更新用户角色")
    @PutMapping("/doAssign")
    public R doAssign(
            @Parameter(description = "用户更新的角色信息",required = true)
            @RequestBody AssignRoleVo assignRoleVo
    ){
        sysRoleService.doAssign(assignRoleVo);
        return R.ok();
    }

}

