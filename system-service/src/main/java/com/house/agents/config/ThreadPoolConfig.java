package com.house.agents.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Thread pool configuration using JDK 21 Virtual Threads.
 * Virtual threads provide better performance and scalability compared to traditional platform threads.
 * They are lightweight and can handle millions of concurrent operations without blocking platform threads.
 */
@Configuration
public class ThreadPoolConfig {  
    
    /**
     * Creates an ExecutorService using virtual threads (JDK 21+ feature).
     * Virtual threads are much more lightweight than platform threads and can handle
     * high concurrency scenarios more efficiently.
     * 
     * This replaces the traditional ThreadPoolExecutor to leverage JDK 21's virtual thread capabilities.
     * 
     * Note: Virtual thread executors do not need explicit shutdown. They are automatically managed
     * by the JVM and do not hold resources that need to be released like traditional thread pools.
     * 
     * @return ExecutorService backed by virtual threads
     */
    @Bean
    public ExecutorService executorService() {  
        // Use virtual threads for better async performance
        // Virtual threads are created on-demand and don't require pre-configured pool sizes
        return Executors.newVirtualThreadPerTaskExecutor();
    }  
}