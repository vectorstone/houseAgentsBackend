package com.house.agents.config;

import com.house.agents.handler.AsyncExceptionHandler;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * @Description:
 * @Author: Gavin
 * @Date: 8/30/2023 8:14 PM
 */
//配置统一异常处理器
@Configuration
public class AsyncConfig implements AsyncConfigurer {
    //这个方法是用来配置线程池的方法,也可以在配置文件里面配置线程池
    //区别就是,在这个方法里面配置的线程池在整个项目里面都可以使用
    //而在配置文件里面配置的线程池只能用于异步任务,推荐在这个方法里面配置线程池
    @Autowired
    private AsyncExceptionHandler asyncExceptionHandler;

    @Autowired
    private ExecutorService executorService;
    /**
     * 配置线程池
     * @return
     */
    @Override
    public Executor getAsyncExecutor() {
        return executorService;
    }

    /**
     * 配置异常处理器
     * @return
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return asyncExceptionHandler;
    }
}
