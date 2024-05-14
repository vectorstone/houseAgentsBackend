package com.house.agents.service.impl;

import com.house.agents.service.WxLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WxLoginServiceImpl implements WxLoginService {
    // 1. 第三方发起微信授权登录请求，微信用户允许授权第三方应用后，微信会拉起应用或重定向到第三方网站，并且带上授权临时票据code参数；
    // 2. 通过code参数加上AppID和AppSecret等，通过API换取access_token；
    // 3. 通过access_token进行接口调用，获取用户基本数据资源或帮助用户实现基本操作。
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private UserLoginRecordService userLoginRecordService;
    @Override
    public String login(HttpSession session) {

        try {
            // https://open.weixin.qq.com/connect/qrconnect?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=STATE#wechat_redirect
            // 1.生成一个state,该参数可用于防止csrf攻击
            String state = UUID.randomUUID().toString().replace("-", "");
            // 2.将存入session会话域中,在callback回调的方法中我们需要对比两次state的状态,用来确保安全
            session.setAttribute("state",state);
            // 3.进行重定向的url进行转义: url转义其实也只是为了符合url的规范而已。因为在标准的url规范中中文和很多的字符是不允许出现在url中的。
            // http://localhost:8160/api/core/wx/callback
            String encodeRedirectURL = URLEncoder.encode(/* "http://localhost:8160/api/core/wx/callback" */WxLoginProperties.REDIRECT_URI, "UTF-8");
            System.out.println(encodeRedirectURL);
            // 4.让浏览器的请求重定向,重定向到二维码扫码登录的页面
            return "redirect:" + WxLoginProperties.QRCONNECT_URL +
                    // appid : 代表在wx平台注册的应用的id
                    "?appid=" + WxLoginProperties.APP_ID +
                    // redirect_uri : wx用户授权后的重定向的地址,需要使用URLEncoder进行转义,避免不合规范
                    "&redirect_uri=" +encodeRedirectURL+
                    // 根据微信的开发者指南,这个地方就填code卡壳
                    "&response_type=code" +
                    // scope 授权的作用域 应用授权作用域，拥有多个作用域用逗号（,）分隔，网页应用目前仅填写snsapi_login即可
                    "&scope=snsapi_login" +
                    // state : 防止恶意攻击的字符串,授权后会出入到回调的接口方法中
                    "&state="+state+"#wechat_redirect";
            // System.out.println(url);
            // return url;
        } catch (UnsupportedEncodingException e) {
            log.error("url转义出现异常,异常信息为:{}", ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    @Override
    public String callback(String code, String state, HttpSession session, HttpServletRequest request) {
        try {
            System.out.println("code = " + code);
            // 1.校验state
            // 1.1从session中获取到的原生的state
            Object originState =  session.getAttribute("state");
            // 1.2.1将获得的原生的state和请求参数中的state进行对比,如果校验失败,直接抛异常
            // 1.2.2除了校验state之外,还要校验code不能为空
            // 1.2.3获取到的state本身也不能为空
            Asserts.AssertTrue(originState != null && StringUtils.equals(originState.toString(),state)
                    && StringUtils.isNotBlank(code), ResponseEnum.WEIXIN_CALLBACK_PARAM_ERROR);

            // 2.根据回传回来的code,获取accessToken
            // https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
            // 2.1设置需要的请求参数
            Map<String,String> params = new HashMap<>();
            params.put("appid",WxLoginProperties.APP_ID);
            params.put("secret",WxLoginProperties.SECRET);
            params.put("code",code);
            params.put("grant_type","authorization_code");
            // 2.2创建httpClientUtils对象,用来发起请求的对象
            HttpClientUtils httpClientUtils = new HttpClientUtils(WxLoginProperties.ACCESS_TOKEN_URL,params);
            // 2.3调用get方法,发起请求(通过浏览器发起的请求,都是get方式的)
            httpClientUtils.get();
            // 2.4获取响应结果
            String content = httpClientUtils.getContent();
            // 2.5将响应转为map集合
            Map<String,Object> map = JSON.parseObject(content, Map.class);
            // 2.6判断accessToken获取成功还是失败(如果响应中包含errcode,那么说明获取失败,直接抛异常)
            Asserts.AssertNotTrue(map.containsKey("errcode"),ResponseEnum.WEIXIN_FETCH_ACCESSTOKEN_ERROR);
            // 2.7取出获取到的accessToken和openid(这个后面会用到)
            // openid对应唯一的一个wx用户,wx用户第一次使用wx登录的时候,我们会将其信息保存到数据库中,后续可以使用openid查询用户
            String accessToken = (String)map.get("access_token");
            String openId = (String)map.get("openid");

            // 获取到openid之后, 要去数据库查询一下,确认下该用户是第一次登录还是之前有登录过
            /**
             * 这里有三种情况:
             * 1.用户第一次使用微信登录,那么直接将用户信息插入到数据库中
             * 2.用户不是第一次使用微信登录,但是距离第一次登录已经超过了3天,所以需要更新用户的头像和昵称信息
             * 3.用户不是第一次使用微信登录,但是距离第一次登录还没要超过3天,所以可以直接数据库中的数据,不需要去微信平台获取用户的信息数据了
             */
            UserInfo userInfo = userInfoService.getOne(Wrappers.lambdaQuery(UserInfo.class).eq(UserInfo::getOpenid, openId));
            if(userInfo == null){
                // 进来这里面,说明用户是第一次登录,需要将用户信息插入到数据库中,对应情况1
                Map<String, Object> userInfoMap = getUserInfoMap(map, accessToken, openId);
                // 3.7将用户的相关信息取出
                String headimgurl = userInfoMap.get("headimgurl").toString();
                String nickName = userInfoMap.get("nickname").toString();

                // 4.将用户信息保存到数据库中
                userInfo = new UserInfo();
                // 4.1设置用户的头像
                userInfo.setHeadImg(headimgurl);
                // 4.2设置用户的nickName
                userInfo.setNickName(nickName);
                // 4.3设置用户的openId
                userInfo.setOpenid(openId);
                // 4.4保存用户信息
                userInfoService.save(userInfo);

            }else if(userInfo != null && System.currentTimeMillis() - userInfo.getUpdateTime().getTime() >= 3*24*60*60*1000){
                // 进来这里面,说明用户不是第一次登录,而且距离上次登录已经超过了3天时间,需要更新用户的头像和昵称,对应情况2
                Map<String, Object> userInfoMap = getUserInfoMap(map, accessToken, openId);
                // 3.7将用户的相关信息取出
                String headimgurl = userInfoMap.get("headimgurl").toString();
                String nickName = userInfoMap.get("nickname").toString();
                userInfo.setNickName(nickName);
                userInfo.setHeadImg(headimgurl);
                // 更新用户的信息到数据库中
                userInfoService.updateById(userInfo);
            }
            // 除此之外的情况,就是情况3

            // 保存用户的登录的日志
            userLoginRecordService.saveLoginRecord(userInfo,request);

            // 5.生成token,并且将浏览器的页面重定向到主页,携带token
            String token = JwtUtils.createToken(userInfo.getId(), userInfo.getNickName());
            return "redirect:" + WxLoginProperties.SRB_INDEX_PAGE_URL + "?token=" +token;

        } catch (Exception e) {
            // 放大异常,使用Exception来接收
            // 打印异常信息
            log.error("调用微信登录失败,异常信息为:{}", ExceptionUtils.getStackTrace(e));
            // 这里就不再抛出异常了,而是直接返回一个前端的异常页面的地址,否则用户会看到服务器返回的异常信息,比较的尴尬
            // throw new RuntimeException(e);
            return "redirect:http://localhost:3000/error?errorCode=101";
        }
    }

    private Map<String, Object> getUserInfoMap(Map<String, Object> map, String accessToken, String openId) throws IOException, ParseException {
        // 3.根据accessToken,获取用户相关的数据
        // https://api.weixin.qq.com/sns/userinfo?access_token=
        // 3.1准备获取用户信息的请求参数
        Map<String,String> params1 = new HashMap<>();
        params1.put("access_token", accessToken);
        params1.put("openid", openId);
        // 3.2准备httpClientUtils对象
        HttpClientUtils httpClientUtils1 = new HttpClientUtils(WxLoginProperties.USER_INFO_URL,params1);
        // 3.3发起请求,获取用户信息
        httpClientUtils1.get();
        // 3.4获取响应信息
        String content1 = httpClientUtils1.getContent();
        // 3.5将响应的结果转化为map集合
        Map<String,Object> userInfoMap = JSON.parseObject(content1, Map.class);
        // 获取到的结果为: userInfoMap = {country=, unionid=oWgGz1MqYanelHR2WHWN40iGTRZc, province=, city=, openid=o3_SC50uEczswGRCb4qwWze5U2cE, sex=0, nickname=今夜有汉堡, headimgurl=https://thirdwx.qlogo.cn/mmopen/vi_32/zlngULMfpcjvCpTqyBVrbibrPhRmTUxF14bYWGr92Zy65HS0Q5yEOTpickMyqPz12LbSibw0xU9G0Gor9YVskRSHg/132, language=, privilege=[]}
        // System.out.println("userInfoMap = " + userInfoMap);
        // 3.6判断一下是否正确的获得了用户的信息
        Asserts.AssertNotTrue(map.containsKey("errcode"),ResponseEnum.WEIXIN_FETCH_USERINFO_ERROR);
        return userInfoMap;
    }
}