package com.house.agents.service;

import com.house.agents.entity.SysRole;
import com.house.agents.entity.vo.AssignRoleVo;
import com.house.agents.entity.vo.SysRoleQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @Description:
 * @Author: Gavin
 * @Date: 6/6/2023 4:53 PM
 */
public interface SysRoleService extends IService<SysRole> {
    Page<SysRole> selectPage(Page<SysRole> pageParam, SysRoleQueryVo sysRoleQueryVo);

    // 查询用户的角色
    Map<String, Object> getRolesByUserId(String userId);

    // 更新用户角色信息
    void doAssign(AssignRoleVo assignRoleVo);
}