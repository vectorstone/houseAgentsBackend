package com.house.agents.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.house.agents.entity.House;
import com.house.agents.entity.ShareEntity;
import com.house.agents.entity.Subway;

import java.util.List;

public interface ShareEntityService extends IService<ShareEntity> {
    String batchShareByIds(List<String> houseIds,Long userId);
    List<House> batchGetHousesByShareId(String shareId);
}
