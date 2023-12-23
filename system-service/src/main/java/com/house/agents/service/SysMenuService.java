package com.house.agents.service;

import com.house.agents.entity.SysMenu;
import com.house.agents.entity.vo.AssignMenuVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 菜单表 服务类
 * </p>
 *
 * @author Gavin
 * @since 2023-08-03
 */
public interface SysMenuService extends IService<SysMenu> {
    List<SysMenu> findNodes();

    void removeMenuById(Long id);

    //根据角色获取菜单
    List<SysMenu> findSysMenuByRoleId(Long roleId);

    //给角色分配菜单
    void doAssign(AssignMenuVo assignMenuVo);
}
