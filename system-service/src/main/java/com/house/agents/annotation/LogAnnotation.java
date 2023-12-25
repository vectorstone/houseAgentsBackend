package com.house.agents.annotation;



import com.house.agents.commonConst.CommonConst;

import java.lang.annotation.*;

/**
 * 该注解主要用来实现记录用户操作日志的作用 以及输入入参和出参的信息到日志文件里面,方便后期查询日志
 * username
 * userId
 * ip
 * Date
 * 具体的操作(方法名字) methodName or Operation
 * request
 * response
 * performance period 程序运行的时间
 */
@Target(ElementType.METHOD) //这个注解只能应用于方法上
@Retention(RetentionPolicy.RUNTIME)//这个注解的保留策略为保存到运行时
@Documented//使用javadock命令生成文档的时候,会保留这个注解
public @interface LogAnnotation {
    //key的名称
    String keyPrefix() default CommonConst.KEY_PREFIX;
    //为了防止缓存击穿,可以给缓存添加分布式锁,这里可以指定分布式锁的前缀
    String lockPrefix() default CommonConst.LOCK_PREFIX;
    //过期时间设置 单位后面可以再aspect里面设置为分钟
    int expireTime() default 300;
    //过期时间额外的随机值上限,单位同样可以在aspect里面设置为分钟
    int extraExpireTime() default 60;
}
