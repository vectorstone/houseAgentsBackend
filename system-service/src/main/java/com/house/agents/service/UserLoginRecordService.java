package com.house.agents.service;

import javax.servlet.http.HttpServletRequest;

public interface UserLoginRecordService extends IService<UserLoginRecord> {
    void saveLoginRecord(UserInfo userInfo, HttpServletRequest request);

    Map<String, Object> loginAndRegistryCount(String start, String end);
}
