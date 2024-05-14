package com.house.agents.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Description:
 * @Author: Gavin
 * @Date: 7/10/2023 8:16 PM
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wx.open")
public class WxLoginProperties implements InitializingBean {

    private String qrconnectUrl;
    private String appid;
    private String redirect_uri;
    private String secret;
    private String accessTokenUrl;
    private String srbIndexPageUrl;
    private String userInfoUrl;
    public static String QRCONNECT_URL;
    public static String APP_ID;
    public static String REDIRECT_URI;
    public static String SECRET;
    public static String ACCESS_TOKEN_URL;
    public static String SRB_INDEX_PAGE_URL;
    public static String USER_INFO_URL;

    @Override
    public void afterPropertiesSet() throws Exception {
        QRCONNECT_URL = this.qrconnectUrl;
        APP_ID = this.appid;
        REDIRECT_URI = this.redirect_uri;
        SECRET = this.secret;
        ACCESS_TOKEN_URL = this.accessTokenUrl;
        SRB_INDEX_PAGE_URL = this.srbIndexPageUrl;
        USER_INFO_URL = this.userInfoUrl;
    }
}
