package com.house.agents.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

public interface WxLoginService {
    String login(HttpSession session);

    String callback(String code, String state, HttpSession session, HttpServletRequest request);

    Map<String,Object> wxLogin(String code, HttpServletRequest request);
}