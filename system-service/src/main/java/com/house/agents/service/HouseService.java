package com.house.agents.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.house.agents.entity.House;
import com.house.agents.entity.vo.HouseSearchVo;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Gavin
 * @since 2023-07-28
 */
public interface HouseService extends IService<House> {

    void importHouses(MultipartFile file, Long userId);

    Page getPageList(Integer pageNum, Integer pageSize, HouseSearchVo houserSearchVo, Long id);
//
//    void exportAccount(HttpServletResponse response, Long userId);
//
//    Map<String, List<String>> getMonthlyCost(String start, String end, Long userId);
//
//    Map<String, List<String>> getLargeAreaData(Long userId);
}
