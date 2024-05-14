package com.house.agents.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public interface WxLoginService {
    String login(HttpSession session);

    String callback(String code, String state, HttpSession session, HttpServletRequest request);
}