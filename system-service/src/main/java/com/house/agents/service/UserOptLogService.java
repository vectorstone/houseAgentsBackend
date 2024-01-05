package com.house.agents.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.house.agents.entity.House;
import com.house.agents.entity.SysUser;
import com.house.agents.entity.UserOptLog;
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
public interface UserOptLogService extends IService<UserOptLog> {
    void saveLog(UserOptLog userOptLog);

}
