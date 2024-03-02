package com.house.agents.security;

import com.house.agents.entity.SysUser;
import com.house.agents.utils.CookieUtils;
import com.house.agents.utils.MutableHttpServletRequest;
import com.house.agents.utils.Result;
import com.house.agents.utils.ResultCodeEnum;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


import java.io.IOException;
import java.lang.reflect.Field;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;
import org.apache.tomcat.util.http.MimeHeaders;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

/**
 * <p>
 * 认证解析token过滤器
 * </p>
 */
// @Component//自己加的component,目的是为了使用SysUserService,用来获取用户对象
//过滤器好像不能直接注入到容器,然后使用@Resource装配
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private RedisTemplate redisTemplate;

    public TokenAuthenticationFilter(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    /* @Resource
    SysUserService sysUserService; */

    // public TokenAuthenticationFilter(){}
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String requestURI = request.getRequestURI();
        logger.info("uri:"+ requestURI);

        // 转换request
        // MutableHttpServletRequest request = new MutableHttpServletRequest(req);

        //如果是登录的接口或者是批量分享房源的接口，直接放行
        if("/admin/user/login".equals(requestURI) || "/admin/house/shareHouse".equals(requestURI)/*|| "/api/oss/upload/".equals(request.getRequestURI())*/ ) {
            chain.doFilter(request, response);
            return;
        }

        //获取认证信息
        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        //如果获取到认证信息，那么我们就通过工具类将认证信息保存在上下文对象中。这样后面的组件就直接从上下文对象中可以获取认证对象
        if(null != authentication) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            //放行请求
            chain.doFilter(request, response);
        } else {
            //如果没有获取到认证信息，说明非法，则抛出异常：你无权访问
            ResponseUtil.out(response, Result.build(null, ResultCodeEnum.PERMISSION));
            // ResponseUtil.out(response, R.setResult(ResponseEnum.PERMISSION));
        }
    }

    private void modifyRequestHeader(HttpServletRequest req,String headerKey,String headerValue) {
        try {
            // 从 RequestFacade 中获取 org.apache.catalina.connector.Request
            Field connectorField = ReflectionUtils.findField(RequestFacade.class, "request", Request.class);
            connectorField.setAccessible(true);
            Request connectorRequest = (Request) connectorField.get(req);

            // 从 org.apache.catalina.connector.Request 中获取 org.apache.coyote.Request
            Field coyoteField = ReflectionUtils.findField(Request.class, "coyoteRequest", org.apache.coyote.Request.class);
            coyoteField.setAccessible(true);
            org.apache.coyote.Request coyoteRequest = (org.apache.coyote.Request) coyoteField.get(connectorRequest);

            // 从 org.apache.coyote.Request 中获取 MimeHeaders
            Field mimeHeadersField =  ReflectionUtils.findField(org.apache.coyote.Request.class, "headers", MimeHeaders.class);
            mimeHeadersField.setAccessible(true);
            MimeHeaders mimeHeaders =  (MimeHeaders) mimeHeadersField.get(coyoteRequest);

            this.mineHeadersHandle(mimeHeaders,headerKey,headerValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void mineHeadersHandle (MimeHeaders mimeHeaders,String headerKey,String headerValue) {
        // 添加一个Header，随机生成请求ID
        mimeHeaders.addValue(headerKey).setString(headerValue);
        // 移除一个header
        // mimeHeaders.removeHeader("User-Agent");
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        // 从请求头中获取token
        String token = request.getHeader("token");

        // 由于文件上传的请求没有经过前端的拦截器,所以请求头里面没有对应的token,所以这个时候尝试从cookie里面获取对应的token
        if (StringUtils.isEmpty(token)){
            token = CookieUtils.getCookieValue(request, "vue_admin_template_token");
            // 如果是通过这种方式获取到的token,那么需要再手动的将token设置到request header里面
            // modifyRequestHeader(request,"token",token);
        }

        if (!StringUtils.isEmpty(token)) {
            //从redis中读取对应的sysUser信息
            // Long userId = JwtUtils.getUserId(token);
            // SysUser sysUser = sysUserService.getById(userId);
            SysUser sysUser = (SysUser)redisTemplate.boundValueOps(token).get();
            if (null != sysUser) {
                //获取sysUser的权限信息（目前我们在登录验证的时候还没有去查询权限信息，所以此处一定是走else）
                if (null != sysUser.getUserPermsList() && sysUser.getUserPermsList().size() > 0) {
                    List<SimpleGrantedAuthority> authorities = sysUser.getUserPermsList().stream().filter(code -> !StringUtils.isEmpty(code.trim())).map(code -> new SimpleGrantedAuthority(code.trim())).collect(Collectors.toList());
                    //返回一个认证之后包含权限的对象
                    return new UsernamePasswordAuthenticationToken(sysUser.getUsername(), null, authorities);
                } else {
                    //返回一个认证之后没有权限的对象
                    return new UsernamePasswordAuthenticationToken(sysUser.getUsername(), null,Collections.emptyList());
                }
            }
        }
        return null;
    }
}
