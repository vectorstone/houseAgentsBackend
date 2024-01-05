package com.house.agents.handler;

import com.alibaba.fastjson.JSON;
import com.house.agents.commonConst.CommonConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @Description:
 * @Author: Gavin
 * @Date: 8/30/2023 8:12 PM
 */
@Component
@Slf4j
public class AsyncExceptionHandler extends SimpleAsyncUncaughtExceptionHandler {
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        /* log.error("异步任务出现了异常,方法是:{},异常原因是:{},参数为:{}"
                ,method.getName(),ex.getMessage(), Arrays.asList(params)); */
        //需要将捕获到的异常里面的userId
        //同步有异常的数据会被存放到redis中,通过定时任务定期的来同步这些异常的信息
        stringRedisTemplate.boundSetOps(CommonConst.EXCEPTION_KEY).add(JSON.toJSONString(params[0]));
    }
}
