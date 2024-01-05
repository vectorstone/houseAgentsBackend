package com.house.agents.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.house.agents.entity.*;
import com.house.agents.entity.vo.HouseSearchVo;
import com.house.agents.entity.vo.HouseVo;
import com.house.agents.listener.HouseExcelDataListener;
import com.house.agents.mapper.HouseMapper;
import com.house.agents.mapper.UserOptLogMapper;
import com.house.agents.result.ResponseEnum;
import com.house.agents.service.HouseAttachmentService;
import com.house.agents.service.HouseService;
import com.house.agents.service.SysUserService;
import com.house.agents.service.UserOptLogService;
import com.house.agents.utils.Asserts;
import com.house.agents.utils.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Gavin
 * @since 2023-07-28
 */
@Service
@Slf4j
public class UserOptLogServiceImpl extends ServiceImpl<UserOptLogMapper, UserOptLog> implements UserOptLogService {

    @Autowired
    UserOptLogMapper userOptLogMapper;
    @Async
    @Override
    public void saveLog(UserOptLog userOptLog) {
        userOptLogMapper.insert(userOptLog);
    }
}
