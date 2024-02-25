package com.house.agents.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.house.agents.annotation.LogAnnotation;
import com.house.agents.commonConst.CommonConst;
import com.house.agents.entity.SysUser;
import com.house.agents.entity.UserOptLog;
import com.house.agents.service.UserOptLogService;
import com.house.agents.utils.XMDLogFormat;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.protocol.HTTP;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: Gavin
 * @Date: 12/25/2023 1:36 PM
 */
//切面类,用来给自定义注解赋能
@Aspect
@Component
@Slf4j
public class LogAspect {
    @Autowired
    // StringRedisTemplate stringRedisTemplate;
    private RedisTemplate redisTemplate; // 使用自己配置好序列化器的redis的template
    // @Autowired
    // RedissonClient redissonClient;
    // @Autowired
    // RBloomFilter rBloomFilter;
    // @Autowired
    // PmsFeign pmsFeign;
    @Autowired
    private UserOptLogService userOptLogService;

    @Autowired
    private ExecutorService executorService;

    //预留一个位置,切点表达式的重用,这个切点表达式会对service里面的所有的方法起作用
    // @Pointcut(value = "execution(* com.house.gmall.index.service.*.*(..))")

    //使用下面的这个切点表达是,那么就只对加了@LogAnnotation注解的方法起作用
    @Pointcut("@annotation(com.house.agents.annotation.LogAnnotation)")
    /* @Pointcut("@annotation(com.atguigu.gmall.index.annotation.GmallCache)")
    public void gmallCachePoint(){} */
    //第一个* 表示权限修饰符和返回值任意
    //第二个* 表示类名任意
    //第三个* 表示方法名任意
    public void pointcutMethod(){}


    //环绕通知
    @Around(value = "pointcutMethod()")
    public Object aroundMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        //环绕通知的几个特点
          // 1.返回值类型必须是object
          // 2.参数的类型必须是ProceedingJoinPoint joinPoint
          // 3.必须声明Throwable类型的异常
          // 4.必须手动的执行目标的方法: jointPoint.proceed(jointPoint.getArgs())
        // 获取目标方法的参数
        Object[] args = joinPoint.getArgs();
        // 获取目标方法的签名 根据这个签名可以获取到目标方法的返回类型已经方法名称之类的信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取目标方法上的注解(因为需要注解里面的相关的一些的参数)
        Method method = signature.getMethod();
        // LogAnnotation annotation = method.getAnnotation(LogAnnotation.class);
        String methodName = method.getName();

        UserOptLog userOptLog = new UserOptLog();

        CompletableFuture<Map<String, Object>> fieldsNameCf = CompletableFuture.supplyAsync(() -> {
            try {
                return getFieldsName(joinPoint);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executorService);



        StopWatch stopWatch = new StopWatch();
        stopWatch.start("分页查询开始");
        // 3.执行目标方法查询数据库
        Object result = joinPoint.proceed(args);
        // 获取响应的结果
        String respParam = postHandle(result);
        stopWatch.stop();

        CompletableFuture<Void> userInfoCf = fieldsNameCf.thenAcceptAsync(t -> {
            // 如何获取用户信息呢
            // Map<String, Object> fieldsName = getFieldsName(joinPoint);
            String token = (String) t.get("token");
            // SysUser sysUser = null;

            // UserOptLog.UserOptLogBuilder userOptLogBuilder = null;
            if (StringUtils.isNotEmpty(token)) {
                SysUser sysUser = (SysUser) redisTemplate.boundValueOps(token).get();
                Long userId = Optional.ofNullable(sysUser).map(SysUser::getId).orElse(0L);
                String username = Optional.ofNullable(sysUser).map(SysUser::getUsername).orElse("");
                userOptLog.setUserId(userId);
                userOptLog.setUsername(username);
                // userOptLogBuilder = UserOptLog.builder().userId(userId).username(username);
            }
        },executorService);

        // 获取request的代码不能放到CompletableFuture里面,不然就会出现NPE,猜测是异步任务的时候线程变了,请求自然也不一样了
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        CompletableFuture<Void> logAndIpCf = fieldsNameCf.thenAcceptAsync(t -> {
            // 获取用户的IP地址
            // HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            //IP地址
            String ipAddr = getRemoteHost(request);
            // 获取用户请求的url路径
            String url = request.getRequestURL().toString();
            // 获取用户请求的参数
            // String reqParam = preHandle(joinPoint,request);
            Map<String, String> paramMap = t.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, map -> {
                Object param = map.getValue();
                if (param instanceof HttpServletRequest) {
                    return getRequestPostJson(param);
                } else if (param instanceof MultipartFile){
                    MultipartFile file = (MultipartFile) param;
                    return file.getOriginalFilename() == null ? "file name is empty" : file.getOriginalFilename();
                } else if (param instanceof HttpServletResponse) {
                    return "response里面的结果不好获取到";
                } else {
                    return JSON.toJSONString(param);
                }
            }));
            String reqParam = JSON.toJSONString(paramMap);
            HashMap<String, String> logTagMap = new HashMap<>();
            logTagMap.put("interfaceName", methodName);
            logTagMap.put("url", url);
            log.info(XMDLogFormat.build().putTags(logTagMap).message("request = " + reqParam) + " reponse = " + respParam);
            userOptLog.setIp(ipAddr);
            userOptLog.setOperation(methodName);
        }, executorService);
        // StopWatch '': running time = 1663391100 ns
        String performanceTime = stopWatch.prettyPrint();
        CompletableFuture<Void> setPerformanceCf = CompletableFuture.runAsync(() -> {
            long afterRuntime = -1;
            try {
                if (StringUtils.isNotEmpty(performanceTime)) {
                    int start = performanceTime.indexOf("=") + 2;
                    int end = performanceTime.indexOf(" ns");
                    String time = performanceTime.substring(start, end);
                    long runtime = Long.parseLong(time);
                    afterRuntime = runtime / 1000000;
                }
                userOptLog.setPerformanceTime(afterRuntime + "ms");
            } catch (NumberFormatException e) {
                log.info(XMDLogFormat.build().putTag("interfaceName", "aroundMethod").message(e.getMessage()));
            }
        }, executorService);
        CompletableFuture.allOf(userInfoCf,logAndIpCf,setPerformanceCf).join();
        // CompletableFuture.allOf(userInfoCf,setPerformanceCf).join();
        // UserOptLog userOptLog = userOptLogBuilder.ip(ipAddr).operation(methodName).request("reqParam").response("respParam").performanceTime(afterRuntime + "ms").build();
        // 将保存日志的操作修改为异步的方式
        // userOptLogService.save(userOptLog);
        userOptLogService.saveLog(userOptLog);

        return result;
    }

    private String getRequestPostJson(Object req){
        try {
            HttpServletRequest request = (HttpServletRequest) req;
            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String reqBody = URLDecoder.decode(sb.toString(), HTTP.UTF_8);
            log.info("Request Body:" + reqBody);
            log.info("[getRequestPostJson get request body with json success]");
            return reqBody;
        } catch (Exception e) {
            log.info("[getRequestPostJson get request body with json fail.Exception message: " + e.getMessage() + "]");
            return "";
        }
    }

    /**
     * 返回的map里面 key 是参数名称 value 是参数值
     * @param joinPoint
     * @return
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     *
     */
    private static Map<String, Object> getFieldsName(ProceedingJoinPoint joinPoint) throws ClassNotFoundException, NoSuchMethodException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        // 参数值
        Object[] args = joinPoint.getArgs();
        Class<?>[] classes = new Class[args.length];
        for (int k = 0; k < args.length; k++) {
            // 不管是封装类型还是基础类型的,我都要
            if (args[k] != null) {
                String result = args[k].getClass().getName();
                Class s = map.get(result);
                classes[k] = s == null ? args[k].getClass() : s;
                // if (!args[k].getClass().isPrimitive()) {
                //     // 获取的是封装类型而不是基础类型
                //     String result = args[k].getClass().getName();
                //     Class s = map.get(result);
                //     classes[k] = s == null ? args[k].getClass() : s;
                // }
            }
        }
        ParameterNameDiscoverer pnd = new DefaultParameterNameDiscoverer();
        // 获取指定的方法，第二个参数可以不传，但是为了防止有重载的现象，还是需要传入参数的类型
        // Method method = Class.forName(classType).getMethod(methodName, classes);

        // 参数名
        String[] parameterNames = pnd.getParameterNames(method);
        // 通过map封装参数和参数值
        HashMap<String, Object> paramMap = new HashMap();
        for (int i = 0; i < parameterNames.length; i++) {
            paramMap.put(parameterNames[i], args[i]);
        }
        return paramMap;
    }

    private static HashMap<String, Class> map = new HashMap<String, Class>() {
        {
            put("java.lang.Integer", int.class);
            put("java.lang.Double", double.class);
            put("java.lang.Float", float.class);
            put("java.lang.Long", long.class);
            put("java.lang.Short", short.class);
            put("java.lang.Boolean", boolean.class);
            put("java.lang.Char", char.class);
            put("java.lang.String",String.class);
        }
    };

    /**
     * 入参数据 231225这个方法里面无法获取到对应的请求参数
     * @param joinPoint
     * @param request
     * @return
     */
    private String preHandle(ProceedingJoinPoint joinPoint,HttpServletRequest request) {

        ArrayList<String> reqparams = Lists.newArrayList();
        String reqParam = "";
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method targetMethod = methodSignature.getMethod();
        Annotation[] annotations = targetMethod.getAnnotations();
        for (Annotation annotation : annotations) {
            // 此处可以改成自定义的注解
            if (annotation.annotationType().equals(LogAnnotation.class)) {
                reqParam = JSON.toJSONString(request.getParameterMap());
                break;
            }
        }
        return reqParam;
    }

    /**
     * 返回数据
     * @param retVal
     * @return
     */
    private String postHandle(Object retVal) {
        if(null == retVal){
            return "";
        }
        return JSON.toJSONString(retVal);
    }


    /**
     * 获取目标主机的ip
     * @param request
     * @return
     */
    private String getRemoteHost(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

    /**
     * join方法可以拼接数组里面的各个元素,前提是数组必须是String类型的
     * @param args
     */
    public static void main(String[] args) {
        String[] arr = {"1", "2","3","4","5"};
        String join = StringUtils.join(arr, ",");
        System.out.println(join);
    }

    // 获取方法的参数的名称和value的方法 https://developer.aliyun.com/article/652997
    // private static Map getFieldsName(ProceedingJoinPoint joinPoint) throws ClassNotFoundException, NoSuchMethodException {
    //     String classType = joinPoint.getTarget().getClass().getName();
    //     String methodName = joinPoint.getSignature().getName();
    //     // 参数值
    //     Object[] args = joinPoint.getArgs();
    //     Class<?>[] classes = new Class[args.length];
    //     for (int k = 0; k < args.length; k++) {
    //         if (!args[k].getClass().isPrimitive()) {
    //             // 获取的是封装类型而不是基础类型
    //             String result = args[k].getClass().getName();
    //             Class s = map.get(result);
    //             classes[k] = s == null ? args[k].getClass() : s;
    //         }
    //     }
    //     ParameterNameDiscoverer pnd = new DefaultParameterNameDiscoverer();
    //     // 获取指定的方法，第二个参数可以不传，但是为了防止有重载的现象，还是需要传入参数的类型
    //     Method method = Class.forName(classType).getMethod(methodName, classes);
    //     // 参数名
    //     String[] parameterNames = pnd.getParameterNames(method);
    //     // 通过map封装参数和参数值
    //     HashMap<String, Object> paramMap = new HashMap();
    //     for (int i = 0; i < parameterNames.length; i++) {
    //         paramMap.put(parameterNames[i], args[i]);
    //     }
    //     return paramMap;
    // }
    //
    // private static HashMap<String, Class> map = new HashMap<String, Class>() {
    //     {
    //         put("java.lang.Integer", int.class);
    //         put("java.lang.Double", double.class);
    //         put("java.lang.Float", float.class);
    //         put("java.lang.Long", long.class);
    //         put("java.lang.Short", short.class);
    //         put("java.lang.Boolean", boolean.class);
    //         put("java.lang.Char", char.class);
    //     }
    // };
}
