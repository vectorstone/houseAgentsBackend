package com.house.agents.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.house.agents.annotation.LogAnnotation;
import com.house.agents.entity.*;
import com.house.agents.entity.vo.HouseSearchVo;
import com.house.agents.result.R;
import com.house.agents.result.ResponseEnum;
import com.house.agents.service.HouseService;
import com.house.agents.service.SubwayService;
import com.house.agents.utils.BusinessException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Gavin
 * @since 2023-07-28
 */
@RestController
@RequestMapping("/admin/house")
@Api(tags = "待出租房模块")
@CrossOrigin
@Slf4j
public class HouseController {
    @Autowired
    private HouseService houseService;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SubwayService subwayService;

    @PreAuthorize("hasAnyAuthority('bnt.house.excelUpload')")
    @ApiOperation("待出租房excel表格的上传功能")
    @PostMapping("/import")
    public R importHouse(@RequestParam("file") MultipartFile file, @RequestHeader("token") String token) {
        // excel也是只能上传自己的账单数据,不能上传别人的数据
        SysUser sysUser = (SysUser) redisTemplate.boundValueOps(token).get();
        Long userId = sysUser.getId();
        houseService.importHouses(file, userId);
        return R.ok();
    }

    @LogAnnotation
    @PreAuthorize("hasAnyAuthority('bnt.house.list')")
    @ApiOperation("分页查询")
    @PostMapping("/{pageNum}/{pageSize}")
    public R getList(/* @RequestParam(value = "searchVo",required = false) SearchVo searchVo */
            @RequestBody HouseSearchVo houseSearchVo,
            @PathVariable("pageNum") Integer pageNum,
            @PathVariable("pageSize") Integer pageSize,
            @RequestHeader("token") String token) {
        // 统计该方法的运行时长
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("分页查询开始");

        // 所有的账单的查询必须只能查询自己的,实现多用户的账单数据的隔离
        SysUser sysUser = (SysUser) redisTemplate.boundValueOps(token).get();
        Long userId = sysUser.getId();
        Page page = houseService.getPageList(pageNum, pageSize, houseSearchVo, sysUser);

        stopWatch.stop();
        log.info("com.house.agents.controller.HouseController.getList运行时长是: " + stopWatch.prettyPrint());
        return R.ok().data("items", page);
    }

    @PreAuthorize("hasAnyAuthority('bnt.house.list')")
    @ApiOperation("分页查询已经下架了的房子信息")
    @PostMapping("/deleted/{pageNum}/{pageSize}")
    public R getDeletedList(/* @RequestParam(value = "searchVo",required = false) SearchVo searchVo */
            @RequestBody HouseSearchVo houseSearchVo,
            @PathVariable("pageNum") Integer pageNum,
            @PathVariable("pageSize") Integer pageSize,
            @RequestHeader("token") String token) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("已经下架了的房子分页查询开始");
        // 所有的账单的查询必须只能查询自己的,实现多用户的账单数据的隔离
        SysUser sysUser = (SysUser) redisTemplate.boundValueOps(token).get();
        Page page = houseService.getDeletedPageList(pageNum, pageSize, houseSearchVo, sysUser);
        stopWatch.stop();
        log.info("com.house.agents.controller.HouseController.getDeletedList运行时长是: " + stopWatch.prettyPrint());
        return R.ok().data("items", page);
    }

    @PreAuthorize("hasAnyAuthority('bnt.house.excelDownload')")
    @ApiOperation("房子信息导出为excel")
    @GetMapping("/export")
    // 这个地方不能有返回值,否则会覆盖服务器给前端的response响应
    public void download(HttpServletResponse response, @RequestHeader(required = true, name = "token") String token) {
        SysUser sysUser = (SysUser) redisTemplate.boundValueOps(token).get();
        Long userId = sysUser.getId();

        houseService.exportHouses(response, userId);
    }

    @PreAuthorize("hasAnyAuthority('bnt.house.remove')")
    @ApiOperation("根据id删除房子")
    @DeleteMapping("/{id}")
    public R removeById(@PathVariable("id") String id, @RequestHeader("token") String token) {
        SysUser sysUser = (SysUser) redisTemplate.boundValueOps(token).get();
        Long userId = sysUser.getId();

        // 加个判断,如果修改的不是自己的账单的数据抛出异常
        House house = houseService.getById(id);
        if (house.getUserId() != userId && !isAdmin(sysUser)) {
            throw new BusinessException(ResponseEnum.NOT_YOUSELF_ACCOUNT);
        }
        houseService.removeById(id);
        return R.ok();
    }

    private boolean isAdmin(SysUser sysUser) {
        List<SysRole> roleList = sysUser.getRoleList();
        if (CollectionUtils.isEmpty(roleList)){
            // 如果roleList为空的话,那么说明是有问题的,需要抛出一场
            log.info(String.format("interfaceName = %s , methodName = %s , parameter = userId : %s","HouseServiceImpl","getPageList", sysUser.getId()));
            throw new BusinessException("roleList为空");
        }
        return roleList.stream().map(SysRole::getRoleCode).anyMatch(roleCode -> roleCode.equals("SYSTEM"));
    }

    @PreAuthorize("hasAnyAuthority('bnt.house.update')")
    @ApiOperation("重新上架房子")
    @PutMapping("/{houseId}")
    public R rePublishHouse(@PathVariable("houseId") String houseId, @RequestHeader("token") String token) {
        SysUser sysUser = (SysUser) redisTemplate.boundValueOps(token).get();
        Long userId = sysUser.getId();
        // 加个判断,如果修改的不是自己的账单的数据抛出异常
        House house = houseService.getByIdDeleted(Long.valueOf(houseId));
        if (house != null && house.getUserId() != userId && !isAdmin(sysUser)) {
            throw new BusinessException(ResponseEnum.NOT_YOUSELF_ACCOUNT);
        }
        houseService.rePublishById(Long.valueOf(houseId));
        return R.ok();
    }

    @PreAuthorize("hasAnyAuthority('bnt.house.update')")
    @ApiOperation("根据id修改房子信息")
    @PutMapping
    public R updateById(@RequestBody House house, @RequestHeader("token") String token) {
        SysUser sysUser = (SysUser) redisTemplate.boundValueOps(token).get();
        Long userId = sysUser.getId();
        // 加个判断,如果修改的不是自己的账单的数据抛出异常
        if (house.getUserId() != userId && !isAdmin(sysUser)) {
            throw new BusinessException(ResponseEnum.NOT_YOUSELF_ACCOUNT);
        }
        houseService.updateById(house);
        return R.ok();
    }

    @PreAuthorize("hasAnyAuthority('bnt.house.add')")
    @ApiOperation("新增房子信息")
    @PostMapping
    public R save(@RequestBody House house, @RequestHeader("token") String token) {
        SysUser sysUser = (SysUser) redisTemplate.boundValueOps(token).get();
        Long userId = sysUser.getId();
        house.setUserId(userId);
        houseService.save(house);
        return R.ok();
    }
    @PreAuthorize("hasAnyAuthority('bnt.house.remove')")
    @ApiOperation("批量删除")
    @DeleteMapping()
    public R batchRemoveByIds(@RequestBody List<String> houseIds, @RequestHeader("token") String token) {
        SysUser sysUser = (SysUser) redisTemplate.boundValueOps(token).get();
        Long userId = sysUser.getId();
        // 加个判断,如果修改的不是自己房子的数据抛出异常
        boolean checkUser = houseIds.stream().map(houseId -> {
            House house = houseService.getById(houseId);
            return house.getUserId();
        }).allMatch(t -> t.equals(userId));
        if (!checkUser && !isAdmin(sysUser)) {
            throw new BusinessException(ResponseEnum.NOT_YOUSELF_ACCOUNT);
        }
        houseService.removeByIds(houseIds);
        return R.ok();
    }

    @PreAuthorize("hasAnyAuthority('bnt.house.update')")
    @ApiOperation("批量重新上架")
    @PutMapping("/batch/republish")
    public R batchRepublishByIds(@RequestBody List<String> houseIds, @RequestHeader("token") String token){
        SysUser sysUser = (SysUser) redisTemplate.boundValueOps(token).get();
        Long userId = sysUser.getId();
        // 加个判断,如果修改的不是自己房子的数据抛出异常
        boolean checkUser = houseIds.stream().map(houseId -> {
            House house = houseService.getByIdDeleted(Long.valueOf(houseId));
            return house.getUserId();
        }).allMatch(t -> t.equals(userId));
        if (!checkUser && !isAdmin(sysUser)) {
            throw new BusinessException(ResponseEnum.NOT_YOUSELF_ACCOUNT);
        }
        houseService.rePublishByIds(houseIds);
        return R.ok();
    }


    @PreAuthorize("hasAnyAuthority('bnt.house.list')")
    @ApiOperation(value = "获取地铁线路信息")
    @GetMapping("/subway")
    public R getSubway(){
        List<Subway> list = subwayService.list();
        return R.ok().data("items",list);
    }
}
