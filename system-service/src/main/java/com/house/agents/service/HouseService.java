package com.house.agents.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.house.agents.entity.House;
import com.house.agents.entity.HouseAttachment;
import com.house.agents.entity.SysUser;
import com.house.agents.entity.vo.HouseSearchVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Gavin
 * @since 2023-07-28
 */
public interface HouseService extends IService<House> {
    List<HouseAttachment> getBannerList();
    List<House> getHouseInfoNoLogin();

    void importHouses(MultipartFile file, Long userId);

    Page getPageList(Integer pageNum, Integer pageSize, HouseSearchVo houserSearchVo, SysUser sysUser);
    Page getDeletedPageList(Integer pageNum, Integer pageSize, HouseSearchVo houserSearchVo, SysUser sysUser);

    //
    void exportHouses(HttpServletResponse response, Long userId);

    House getByIdDeleted(Long houseId);

    void rePublishById(Long houseId);

    void rePublishByIds(List<String> houseIds);

    void setHouseAttachment(List<House> houses,SysUser sysUser);

    House getHouseInfo(String houseId);

}
