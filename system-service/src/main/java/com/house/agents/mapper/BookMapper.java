package com.house.agents.mapper;

import com.house.agents.entity.Book;
import com.house.agents.entity.vo.MonthCostVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Gavin
 * @since 2023-07-28
 */
public interface BookMapper extends BaseMapper<Book> {

    List<MonthCostVo> getMonthlyCost(@Param("start") String start, @Param("end") String end, @Param("userId")Long userId);

    List<Book> getAllData();
}
