package com.house.agents.controller;


import com.house.agents.entity.SysMenu;
import com.house.agents.entity.vo.AssignMenuVo;
import com.house.agents.service.SysMenuService;
import com.house.agents.utils.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 菜单表 前端控制器
 * </p>
 *
 * @author Gavin
 * @since 2023-08-03
 */
@Tag(name = "菜单管理模块")
@RestController
@RequestMapping("/admin/system/sysMenu")
@Transactional
public class SysMenuController {
    @Autowired
    private SysMenuService sysMenuService;

    @PreAuthorize("hasAnyAuthority('bnt.sysMenu.list')")
    @GetMapping("")
    @Operation(summary = "获取菜单")
    public Result<List<SysMenu>> getMenusList(){
        return Result.ok(sysMenuService.findNodes());
    }
    //根据id查询菜单
    //这个方法自始至终好像都没有用上
    @PreAuthorize("hasAnyAuthority('bnt.sysMenu.list')")
    // @PreAuthorize("hasAnyAuthority('bnt.sysMenu.update')")
    @GetMapping("/getById/{id}")
    @Operation(summary = "根据id查询菜单")
    public Result<SysMenu> getById(
            @Parameter(description = "菜单id",required = true)
            @PathVariable("id") Long id
    ){
        SysMenu byId = sysMenuService.getById(id);
        return Result.ok(byId);
    }

    //增加菜单
    @PreAuthorize("hasAnyAuthority('bnt.sysMenu.add')")
    @PostMapping("")
    @Operation(summary = "增加菜单")
    public Result save(
            @Parameter(description = "需要增加的菜单对象",required = true)
            @RequestBody SysMenu permission
    ){
        sysMenuService.save(permission);
        return Result.ok();
    }

    //修改菜单
    @PreAuthorize("hasAnyAuthority('bnt.sysMenu.update')")
    @PutMapping("")
    @Operation(summary = "修改菜单")
    public Result update(
            @Parameter(description = "需要修改的菜单",required = true)
            @RequestBody SysMenu permission
    ){
        sysMenuService.updateById(permission);
        return Result.ok();
    }

    //根据id删除菜单
    @PreAuthorize("hasAnyAuthority('bnt.sysMenu.remove')")
    @DeleteMapping("/{id}")
    @Operation(summary = "根据id删除菜单")
    public Result deleteMenu(
            @Parameter(description = "需要删除的菜单的id",required = true)
            @PathVariable("id") Long id
    ){
        sysMenuService.removeMenuById(id);
        return Result.ok();
    }
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~下面的是角色分配菜单的方法~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //根据角色获取菜单
    @PreAuthorize("hasAnyAuthority('bnt.sysRole.assignAuth')")
    @GetMapping("/toAssign/{roleId}")
    @Operation(summary = "根据角色id获取菜单")
    public Result<List<SysMenu>> toAssign(
            @Parameter(description = "角色id",required = true)
            @PathVariable("roleId") Long roleId
    ){
        List<SysMenu> sysMenuList = sysMenuService.findSysMenuByRoleId(roleId);
        return Result.ok(sysMenuList);

    }

    //给角色分配菜单
    @PreAuthorize("hasAnyAuthority('bnt.sysRole.assignAuth')")
    @Operation(summary = "给角色分配菜单")
    @PostMapping("/doAssign")
    public Result doAssign(
            @Parameter(description = "菜单分配的角色信息对象",required = true)
            @RequestBody AssignMenuVo assignMenuVo)
    {
        sysMenuService.doAssign(assignMenuVo);
        return Result.ok();
    }
}

