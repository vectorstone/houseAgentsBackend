package com.house.agents.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.house.agents.entity.House;
import com.house.agents.entity.ShareEntity;
import com.house.agents.entity.ShareToHouse;
import com.house.agents.entity.Subway;
import com.house.agents.mapper.ShareEntityMapper;
import com.house.agents.mapper.SubwayMapper;
import com.house.agents.result.ResponseEnum;
import com.house.agents.service.*;
import com.house.agents.utils.BusinessException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Service
public class ShareEntityServiceImpl extends ServiceImpl<ShareEntityMapper, ShareEntity> implements ShareEntityService {
    @Autowired
    private ShareToHouseService shareToHouseService;
    @Autowired
    private HouseService houseService;

    @Autowired
    private HouseAttachmentService houseAttachmentService;

    @Autowired
    private ExecutorService executorService;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String batchShareByIds(List<String> houseIds,Long userId) {

        ShareEntity shareEntity = ShareEntity.builder()
                .userId(userId)
                .build();
        // 保存分享实体
        this.save(shareEntity);

        // 保存分享实体和房源的对应关系
        List<ShareToHouse> shareToHouses = houseIds.stream().map(houseId -> {
            ShareToHouse shareToHouse = new ShareToHouse();
            shareToHouse.setShareId(shareEntity.getId());
            shareToHouse.setHouseId(Long.valueOf(houseId));
            return shareToHouse;
        }).collect(Collectors.toList());
        shareToHouseService.saveBatch(shareToHouses);
        return String.valueOf(shareEntity.getId());
    }

    @Override
    public List<House> batchGetHousesByShareId(String shareId) {
        // 先查出来对应的houseId
        List<ShareToHouse> shareToHouses = shareToHouseService.list(Wrappers.lambdaQuery(ShareToHouse.class).eq(ShareToHouse::getShareId, shareId));
        if (CollectionUtils.isEmpty(shareToHouses)) {
            throw new BusinessException(ResponseEnum.SHARE_ERROR);
        }
        List<Long> houseIds = shareToHouses.stream().map(ShareToHouse::getHouseId).filter(Objects::nonNull).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(shareToHouses)) {
            return Lists.newArrayList();
        }
        List<House> houses = houseService.listByIds(houseIds);
        houseService.setHouseAttachment(houses);
        houses.forEach(house -> {
            // 隐藏敏感信息
            house.setKeyOrPassword("");
            house.setRemark("");
        });
        return houses;
    }
}


