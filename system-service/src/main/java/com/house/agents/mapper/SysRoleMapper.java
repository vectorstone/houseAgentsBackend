package com.house.agents.mapper;

import com.house.agents.entity.SysRole;
import com.house.agents.entity.vo.SysRoleQueryVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 角色 Mapper 接口
 * </p>
 *
 * @author Gavin
 * @since 2023-08-03
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {
    Page<SysRole> selectPage(Page<SysRole> pageParam, @Param("vo") SysRoleQueryVo sysRoleQueryVo);

    List<SysRole> getUserRoleListByUserId(Long UserId);
}
