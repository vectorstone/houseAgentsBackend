package com.house.agents.controller;

import com.alibaba.excel.util.StringUtils;
import com.house.agents.result.R;
import com.house.agents.service.OssService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @Description:
 * @Author: Gavin
 * @Date: 7/6/2023 7:41 PM
 */
@RestController
@RequestMapping("/wx")
@Api(tags = "微信公众号平台服务器验证")
public class WeChatVerify {

    @ApiOperation("微信公众号平台服务器验证")
    @GetMapping("")
    public String verifyInformationFromWeChat(
            @RequestParam("signature") String signature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam("echostr") String echostr) throws NoSuchAlgorithmException {
        String token = "sdfgerefefdfcwertdf";
        // 组合
        List<String> list = Arrays.asList(token, timestamp, nonce);
        // 排序
        Collections.sort(list);

        StringBuilder stringBuilder = new StringBuilder();
        list.forEach(stringBuilder::append);

        // 加密
        MessageDigest digest = MessageDigest.getInstance("sha1");
        byte[] digest1 = digest.digest(stringBuilder.toString().getBytes());

        // 转换成signature的格式
        StringBuilder stringBuilder1 = new StringBuilder();
        for (byte b : digest1) {
            stringBuilder1.append(Integer.toHexString((b >> 4) & 15));
            stringBuilder1.append(Integer.toHexString(b & 15));
        }

        // 判断是否来自微信服务器的请求,如果是的话返回echostr,否则返回空
        return StringUtils.equals(signature,stringBuilder1.toString()) ? echostr : "";
    }
}