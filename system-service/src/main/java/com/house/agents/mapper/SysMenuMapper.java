package com.house.agents.mapper;

import com.house.agents.entity.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 菜单表 Mapper 接口
 * </p>
 *
 * @author Gavin
 * @since 2023-08-03
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    List<SysMenu> getMenuByUserId(@Param("userId")Long userId);
}
