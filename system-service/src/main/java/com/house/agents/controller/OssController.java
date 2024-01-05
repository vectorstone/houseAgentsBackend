package com.house.agents.controller;

import com.house.agents.result.R;
import com.house.agents.service.OssService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Description:
 * @Author: Gavin
 * @Date: 7/6/2023 7:41 PM
 */
@RestController
@RequestMapping("/api/oss")
@Api(tags = "oss文件管理模块")
public class OssController {
    @Autowired
    OssService ossService;

    // requestParam方式传文件的方法
    // @PreAuthorize("hasAnyAuthority('bnt.house.attachmentUpload')")
    @PostMapping("/upload")
    @ApiOperation("文件上传requestParam的方式")
    public R upload(MultipartFile file,
                    @RequestParam("houseId") String houseId, HttpServletRequest request, HttpServletResponse response) {
        // file指的是需要上传的文件的对象
        // module指的是模块的名称,也就是bucket里面保存文件的目录名

        String path = ossService.upload(file, houseId,request,response);
        // 将上传成功后的文件的路径返回给前端,用来做回显
        // return Result.ok(path); // TODO 这个地方有修改前端里面需要注意下
        return R.ok(); // TODO 这个地方有修改前端里面需要注意下 暂时不需要将图片的path返回给前端,到时候再统一的查附件的url吧
    }

    // 删除文件的方法 这个接口理论上来说要鉴权
    // @PreAuthorize("hasAnyAuthority('bnt.house.remove')")
    @ApiOperation("删除文件")
    @DeleteMapping
    public R deleteFile(@RequestParam("path") String path,@RequestHeader("token") String token) {
        ossService.deleteByPath(path,token);
        return R.ok();
    }
}
