package com.house.agents.security;

import com.house.agents.annotation.LogAnnotation;
import com.house.agents.entity.SysUser;
import com.house.agents.entity.vo.LoginVo;
import com.house.agents.utils.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: Gavin
 * @Date: 6/14/2023 8:04 PM
 * 登录过滤器，继承UsernamePasswordAuthenticationFilter，对用户名密码进行登录校验
 */
public class TokenLoginFilter extends UsernamePasswordAuthenticationFilter {
    private RedisTemplate redisTemplate;

    public TokenLoginFilter(AuthenticationManager authenticationManager,RedisTemplate redisTemplate){
        this.setAuthenticationManager(authenticationManager);
        //指定登录的接口及提交的方式,可以指定任意路径
        this.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/admin/user/login","POST"));
        //给redisTemplate属性赋值
        this.redisTemplate = redisTemplate;
    }

    /**
     * 登录认证
     * @param req
     * @param res
     * @return
     * @throws AuthenticationException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException {

        try {
            //登录的loginVo对象是以请求体的方式传过来的,所以通过request.getParameter获取不到, 只能通过最原始的流的方式获取相关的数据
            LoginVo loginVo = new ObjectMapper().readValue(req.getInputStream(), LoginVo.class);
            //封装用户名和密码
            Authentication authenticationToken = new UsernamePasswordAuthenticationToken(loginVo.getUsername(),loginVo.getPassword());
            //调用AuthenticationManager的authenticate方法来进行认证
            return this.getAuthenticationManager().authenticate(authenticationToken);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 认证成功需要执行的方法
     * @param request
     * @param response
     * @param chain
     * @param auth
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication auth) throws IOException, ServletException {
        //1.从Authentication中获取用户信息
        MyCustomUser customUser = (MyCustomUser) auth.getPrincipal();

        //2.将用户信息保存到redis中，有效时长2小时
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        SysUser sysUser = customUser.getSysUser();
        redisTemplate.boundValueOps(token).set(sysUser,2, TimeUnit.HOURS);
        //230913 这里不能使用jwt实现无状态登录的,必须要借助与Redis,因为,sysUser里面有一个字段里面封装的是用户的权限信息的集合
        // ,而jwt类型的token里面只有userId和userName,信息不够的全面(其实要想做的话,也不是不可以,只是载荷会非常的大
        //这里使用jwt的方式来生成token
        // String token = JwtUtils.createToken(customUser.getSysUser().getId(), customUser.getSysUser().getName());

        //3.同时将token响应给客户端
        /* Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        ResponseUtil.out(response, R.ok().data(map)); */
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        // 将获取到的用户的信息跟随token一起返回给前端,这样前端不用再查询getUserInfo接口获取对应的信息了
        // 密码信息清空掉
        sysUser.setPassword("");
        map.put("sysUser", sysUser);
        ResponseUtil.out(response, Result.ok(map));
    }

    //认证失败之后需要执行的方法
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        ResponseUtil.out(response,Result.build(null,444,failed.getMessage()));
        // ResponseUtil.out(response,R.error());
    }
}
