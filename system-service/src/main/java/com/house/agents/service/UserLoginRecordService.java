package com.house.agents.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.house.agents.entity.SysUser;
import com.house.agents.entity.UserLoginRecord;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface UserLoginRecordService extends IService<UserLoginRecord> {
    void saveLoginRecord(SysUser sysUser, HttpServletRequest request);

    Map<String, Object> loginAndRegistryCount(String start, String end);
}
