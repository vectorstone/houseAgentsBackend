package com.house.agents.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.house.agents.entity.House;
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
public interface HouseMapper extends BaseMapper<House> {
    House getDeletedHouse(Long houseId);
    int rePublishDeletedHouse(Long houseId);
    int rePublishDeletedHouses(@Param("houseIds") List<String> houseIds);
    Integer getTotalCount(Long houseId);
    List<House> getDeletedHousesWithPage(@Param("userId")Long userId,@Param("limitNum")int limitNum,@Param("pageSize")int pageSize,@Param("userIds")List<Long> userIds);

}
