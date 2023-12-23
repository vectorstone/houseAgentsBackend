package com.house.agents.service.impl;

import com.house.agents.entity.SysRole;
import com.house.agents.security.MyCustomUser;
import com.house.agents.entity.SysUser;
import com.house.agents.service.SysUserService;
import com.house.agents.utils.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;


@Component
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserService sysUserService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserService.getByUsername(username);
        if(null == sysUser) {
            throw new UsernameNotFoundException("用户名不存在！");
        }

        if(sysUser.getStatus() == 0) {
            throw new BusinessException("账号已停用");
        }
        //查询用户的权限集合,然后设置到用户对象中,后面这个对象会存入到redis中,然后spring security可以从redis中获取到关于这个用户的相关的权限的数据
        List<String> userPermsList = sysUserService.getUserBtnPersByUserId(sysUser.getId());
        sysUser.setUserPermsList(userPermsList);
        // 查询用户的角色列表,然后将其设置到用户对象中,后面根据这个角色列表判断是否为管理员,如果为管理员的话,那么可以查询所有其他用户的数据
        List<SysRole> roleList = sysUserService.getUserRoleListByUserId(sysUser.getId());
        sysUser.setRoleList(roleList);
        return new MyCustomUser(sysUser, Collections.emptyList());
    }
}