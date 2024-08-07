package com.house.agents.service;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface OssService {
    String upload(MultipartFile file, String module, HttpServletRequest request, HttpServletResponse response);

    void deleteByPath(String path,String token);

    String uploadAvatar(MultipartFile file, HttpServletRequest request, HttpServletResponse response);
}
