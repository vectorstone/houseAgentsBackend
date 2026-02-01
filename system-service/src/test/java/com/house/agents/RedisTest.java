package com.house.agents;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: Gavin
 * @Date: 8/5/2023 1:45 PM
 */
@SpringBootTest
@Disabled("需要 Redis 服务器，跳过此测试")
public class RedisTest {
    @Resource
    RedisTemplate redisTemplate;
    @Test
    void test1(){
        redisTemplate.boundValueOps("test").set("content",3, TimeUnit.HOURS);
        Object test = redisTemplate.boundValueOps("test").get();
        System.out.println("test = " + test);
    }
}
