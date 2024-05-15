package com.house.agents.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.aliyuncs.exceptions.ClientException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.house.agents.Enum.FileContentTypeEnum;
import com.house.agents.config.OssProperties;
import com.house.agents.entity.House;
import com.house.agents.entity.HouseAttachment;
import com.house.agents.entity.SysUser;
import com.house.agents.mapper.HouseAttachmentMapper;
import com.house.agents.result.ResponseEnum;
import com.house.agents.service.HouseAttachmentService;
import com.house.agents.service.HouseService;
import com.house.agents.service.OssService;
import com.house.agents.service.SysUserService;
import com.house.agents.utils.BusinessException;
import com.house.agents.utils.CookieUtils;
import com.house.agents.utils.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

/**
 * @Description:
 * @Author: Gavin
 * @Date: 7/6/2023 7:46 PM
 */
@Service
//不需要这一步,我们已经在属性的配置类那个地方使用了@Configuration,会创建对象注入IOC容器的,或者也可以使用下面的注解
//指定当前组件类初始化之前必须创建OssProperties对象注入到容器中
// @EnableConfigurationProperties(OssProperties.class)
@Slf4j
public class OssServiceImpl implements OssService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private HouseService houseService;
    @Autowired
    private HouseAttachmentService houseAttachmentService;


    /**
     *
     * @param file 前端传过来的文件
     * @param houseId 这个可以是前端随文件传过来的任意的东西,此处可以是作业名称,或者用户的id或者对应的house的id
     * @param request
     * @param response
     * @return
     */
    @Override
    public String upload(MultipartFile file, String houseId, HttpServletRequest request, HttpServletResponse response) {
        //从请求头中获取token,从而可以知道目前上传图片的用户是哪一个 230907
        // String token = request.getHeader("token"); //这种方式获取不到token,因为前端图片上传里面的请求没有经过前端的拦截器,所以请求头里面没有设置token
        String token = CookieUtils.getCookieValue(request, "vue_admin_template_token");//只能通过cookie的方式获取到对应的token
        SysUser sysUser = (SysUser)redisTemplate.boundValueOps(token).get();
        // 其实如果实在是获取不到token的话,也可以通过前端传过来的houseId查询获取对应的house,然后通过house里面的userId获取到对应的用户的信息,只是这种方式性能会差一点

        //如果获取不到sysUser有可能是用户没有登录或者登录的信息已经过期了,可以抛出异常
        if (sysUser == null){
            //进来这里面说明登录的信息已经过期了或者用户压根就没有登录
            throw new BusinessException(ResultCodeEnum.LOGIN_AUTH.getMessage());
        }

        //1.先定义文件的名字objectName
        //最终拼出来的效果类似于 avatar/2023/07/07/23423423423_xcsdf.jpg
        // String objectName = houseId + "-" + sysUser.getName() +  new DateTime().toString("/yyyy/MM/dd/") +
        //230908 优化文件保存的路径,直接使用作业的名称(houseId),不再使用module + 用户姓名的方式
        String originalFilename = file.getOriginalFilename();
        String objectName = houseId +  new DateTime().toString("/yyyy/MM/dd/") +
                System.currentTimeMillis() + "_" + UUID.randomUUID().toString()
                //包含开始,不包含结束
                .substring(0,6) + originalFilename.substring(originalFilename
                //取文件的后缀名
                .lastIndexOf("."));
        //2.定义文件的存储的路径用来回显
        String path = OssProperties.SCHEMA + OssProperties.BUCKETNAME + "." + OssProperties.ENDPOINT + "/" + objectName;
        OSS ossClient = null;
        //3.创建输入流
        try {
            ossClient = getOssClient();
            InputStream inputStream = file.getInputStream();
            //4.创建PutObjectRequest对象
            PutObjectRequest putObjectRequest = new PutObjectRequest(OssProperties.BUCKETNAME, objectName, inputStream);
            //5.创建PutObjcet请求
            PutObjectResult putObjectResult = ossClient.putObject(putObjectRequest);

            // 根据houseId查询出来house的相关的信息,用来构建HouseAttachment里面的Description的信息
            House house = houseService.getById(houseId);
            String description = house.getCommunity() + "-" + house.getRoomNumber();
            //将文件的路径保存到图片表里面
            HouseAttachment houseAttachment = new HouseAttachment();
            houseAttachment.setUserId(sysUser.getId());
            houseAttachment.setHouseId(Long.parseLong(houseId));
            houseAttachment.setUsername(sysUser.getUsername());
            houseAttachment.setImageName(originalFilename);
            houseAttachment.setUrl(path);
            houseAttachment.setDescription(description);
            // 获取上传的文件的类型,用来做标识
            // video/mp4 file.getContentType()
            // image/png
            String contentType = file.getContentType();
            if (StringUtils.contains(contentType,"video")){
                houseAttachment.setContentType(FileContentTypeEnum.HOUSE_VIDEO.getCode());
            } else if (StringUtils.contains(contentType,"image")){
                houseAttachment.setContentType(FileContentTypeEnum.HOUSE_IMAGE.getCode());
            } else {
                houseAttachment.setContentType(FileContentTypeEnum.HOUSE_OTHER.getCode());
            }

            houseAttachmentService.save(houseAttachment);
            // SysImages sysImages = new SysImages();
            // sysImages.setImageName(houseId);
            // sysImages.setUsername(sysUser.getName());
            // sysImages.setUrl(path);
            // sysImages.setUserId(sysUser.getId());
            // sysImages.setCreateTime(new Date());
            // sysImages.setUpdateTime(sysImages.getCreateTime());
            // userImagesMapper.insert(sysImages);

            //6.返回文件的路径给前端做回显
            return path;
        } catch (Exception e) {
            //打印异常的堆栈信息
            log.error("文件上传失败,失败信息是:{}",ExceptionUtils.getStackTrace(e));
            //抛出我们的自定义异常
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR);
        }finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Override
    public void deleteByPath(String path,String token) {
        //从path中获取objectName,这个是要删除的文件在桶内的完整的路径,下面删除的方法里面需要这个参数
        String objectName = path.replace(OssProperties.SCHEMA + OssProperties.BUCKETNAME + "." + OssProperties.ENDPOINT + "/", "");

        //获取ossClient对象
        OSS ossClient = null;

        try {
            ossClient = getOssClient();
            // 删除文件或目录。如果要删除目录，目录必须为空。
            ossClient.deleteObject(OssProperties.BUCKETNAME, objectName);
            // 对应的,attachment表里面的这个附件也要删除掉
            boolean remove = houseAttachmentService.remove(Wrappers.lambdaUpdate(HouseAttachment.class).eq(HouseAttachment::getUrl, path));

        } catch (OSSException e) {
            //打印异常的堆栈信息
            log.error("文件删除失败,失败信息是:{}",ExceptionUtils.getStackTrace(e));
            //抛出我们的自定义异常
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

    }

    @Override
    public String uploadAvatar(MultipartFile file, HttpServletRequest request, HttpServletResponse response) {
        //从请求头中获取token,从而可以知道目前上传图片的用户是哪一个 230907
        String token = request.getHeader("token"); //这种方式获取不到token,因为前端图片上传里面的请求没有经过前端的拦截器,所以请求头里面没有设置token
        if (StringUtils.isBlank(token)) {
            token = CookieUtils.getCookieValue(request, "vue_admin_template_token");//只能通过cookie的方式获取到对应的token
        }
        if (StringUtils.isBlank(token)) {
            token = CookieUtils.getCookieValue(request, "token");
        }
        if (StringUtils.isBlank(token)) {
            throw new BusinessException(ResultCodeEnum.LOGIN_AUTH.getMessage());
        }
        SysUser sysUser = (SysUser)redisTemplate.boundValueOps(token).get();
        // 其实如果实在是获取不到token的话,也可以通过前端传过来的houseId查询获取对应的house,然后通过house里面的userId获取到对应的用户的信息,只是这种方式性能会差一点

        //如果获取不到sysUser有可能是用户没有登录或者登录的信息已经过期了,可以抛出异常
        if (sysUser == null){
            //进来这里面说明登录的信息已经过期了或者用户压根就没有登录
            throw new BusinessException(ResultCodeEnum.LOGIN_AUTH.getMessage());
        }

        //1.先定义文件的名字objectName
        //最终拼出来的效果类似于 avatar/2023/07/07/23423423423_xcsdf.jpg
        // String objectName = houseId + "-" + sysUser.getName() +  new DateTime().toString("/yyyy/MM/dd/") +
        //230908 优化文件保存的路径,直接使用作业的名称(houseId),不再使用module + 用户姓名的方式
        String originalFilename = file.getOriginalFilename();
        String objectName = "avatar" +  new DateTime().toString("/yyyy/MM/dd/") +
                System.currentTimeMillis() + "_" + UUID.randomUUID().toString()
                //包含开始,不包含结束
                .substring(0,6) + originalFilename.substring(originalFilename
                //取文件的后缀名
                .lastIndexOf("."));
        //2.定义文件的存储的路径用来回显
        String path = OssProperties.SCHEMA + OssProperties.BUCKETNAME + "." + OssProperties.ENDPOINT + "/" + objectName;
        OSS ossClient = null;
        //3.创建输入流
        try {
            ossClient = getOssClient();
            InputStream inputStream = file.getInputStream();
            //4.创建PutObjectRequest对象
            PutObjectRequest putObjectRequest = new PutObjectRequest(OssProperties.BUCKETNAME, objectName, inputStream);
            //5.创建PutObjcet请求
            PutObjectResult putObjectResult = ossClient.putObject(putObjectRequest);

            sysUser.setHeadUrl(path);

            //6.返回文件的路径给前端做回显
            return path;
        } catch (Exception e) {
            //打印异常的堆栈信息
            log.error("文件上传失败,失败信息是:{}",ExceptionUtils.getStackTrace(e));
            //抛出我们的自定义异常
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR);
        }finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    //获取ossClient的方法单独的抽出来
    private OSS getOssClient(){
        //从系统的环境变量里面获取access ID和access sevret用来创建凭证
        try {
            EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
            //创建ossClient的实例
            OSS ossClient = new OSSClientBuilder().build(OssProperties.ENDPOINT,credentialsProvider);
            return ossClient;
        } catch (ClientException e) {
            //打印异常的堆栈信息
            log.error(ExceptionUtils.getStackTrace(e));
            //抛出我们的自定义的异常
            throw new BusinessException(ResponseEnum.WEIXIN_FETCH_ACCESSTOKEN_ERROR);
        }
    }
}
