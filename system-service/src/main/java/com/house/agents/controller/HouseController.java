package com.house.agents.controller;


import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.house.agents.Enum.HouseStatusEnum;
import com.house.agents.annotation.LogAnnotation;
import com.house.agents.entity.*;
import com.house.agents.entity.vo.HouseSearchVo;
import com.house.agents.result.R;
import com.house.agents.result.ResponseEnum;
import com.house.agents.service.HouseService;
import com.house.agents.service.ShareEntityService;
import com.house.agents.service.SubwayService;
import com.house.agents.service.SysUserService;
import com.house.agents.utils.Asserts;
import com.house.agents.utils.BusinessException;
import com.house.agents.utils.CookieUtils;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

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

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private ShareEntityService shareEntityService;


    // @PreAuthorize("hasAnyAuthority('bnt.house.list')")
    @ApiOperation("获取房子的详细的情况")
    @PostMapping("/{houseId}")
    @LogAnnotation
    public R getHouseInfo(@PathVariable("houseId") String houseId, @RequestHeader("token") String token) {
        // excel也是只能上传自己的账单数据,不能上传别人的数据
        SysUser sysUser = validUser(token);
        Long userId = sysUser.getId();
        House house = houseService.getHouseInfo(houseId);
        // Cat.logEvent("getHouseInfo","getHouseInfo");
        if ((house == null || house.getUserId() != userId) && !isAdmin(sysUser)) {
            // 进来这里面就不返回对应的数据
            return R.ok();
        }
        return R.ok().data("item", house);
    }

    @PreAuthorize("hasAnyAuthority('bnt.house.password')")
    @ApiOperation("修改自己的密码")
    @PostMapping("/modifyPassword")
    @LogAnnotation
    public R modifyPassword(@RequestBody MyPasswordVo myPasswordVo , @RequestHeader("token") String token) {
        // excel也是只能上传自己的账单数据,不能上传别人的数据
        SysUser sysUser = validUser(token);
        Long userId = sysUser.getId();
        // House house = houseService.getById(houseId);
        // Cat.logEvent("getHouseInfo","getHouseInfo");
        // if ((house == null || house.getUserId() != userId) && !isAdmin(sysUser)) {
        //     // 进来这里面就不返回对应的数据
        //     return R.ok();
        // }
        Asserts.AssertNotNull(myPasswordVo, ResponseEnum.PASSWORD_EMPTY);
        sysUserService.modifyPassword(userId,myPasswordVo,token);
        return R.ok();
    }

    private SysUser validUser(String token) {
        // Cat.logEvent("validUser","validUser");
        SysUser sysUser = (SysUser) redisTemplate.boundValueOps(token).get();
        Asserts.AssertNotNull(sysUser, ResponseEnum.ARGUMENT_VALID_ERROR);
        return sysUser;
    }

    @PreAuthorize("hasAnyAuthority('bnt.house.excelUpload')")
    @ApiOperation("待出租房excel表格的上传功能")
    @PostMapping("/import")
    @LogAnnotation
    public R importHouse(HttpServletRequest request, @RequestParam("file") MultipartFile file, @RequestHeader(required = false, name = "token") String token) {
        // excel也是只能上传自己的账单数据,不能上传别人的数据
        // Cat.logEvent("importHouse","importHouse");
        if (StringUtils.isBlank(token)) {
            token = CookieUtils.getCookieValue(request, "vue_admin_template_token");
        }
        SysUser sysUser = validUser(token);
        Long userId = sysUser.getId();
        houseService.importHouses(file, userId);
        return R.ok();
    }

    @LogAnnotation
    // @PreAuthorize("hasAnyAuthority('bnt.house.list')")
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
        SysUser sysUser = validUser(token);
        Long userId = sysUser.getId();
        Page page = houseService.getPageList(pageNum, pageSize, houseSearchVo, sysUser);

        // Transaction t = Cat.newTransaction("URL", "pageName");
        // try {
        //     Cat.logEvent("test1","getList");
        //     Cat.logEvent("URL1.Server", "serverIp", Event.SUCCESS, "ip=${serverIp}");
        //     Cat.logMetricForCount("metric1.key");
        //     Cat.logMetricForDuration("metric1.key", 5);
        //
        //     t.setStatus(Transaction.SUCCESS);
        // } catch (Exception e) {
        //     t.setStatus(e);
        //     Cat.logError(e);
        // } finally {
        //     t.complete();
        // }


        stopWatch.stop();
        log.info("com.house.agents.controller.HouseController.getList运行时长是: " + stopWatch.prettyPrint());
        return R.ok().data("items", page);
    }

    @Deprecated
    @PreAuthorize("hasAnyAuthority('bnt.house.list')")
    @ApiOperation("分页查询已经下架了的房子信息")
    @PostMapping("/deleted/{pageNum}/{pageSize}")
    @LogAnnotation
    public R getDeletedList(/* @RequestParam(value = "searchVo",required = false) SearchVo searchVo */
            @RequestBody HouseSearchVo houseSearchVo,
            @PathVariable("pageNum") Integer pageNum,
            @PathVariable("pageSize") Integer pageSize,
            @RequestHeader("token") String token) {
        // Cat.logEvent("getDeletedList","getDeletedList");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("已经下架了的房子分页查询开始");
        // 所有的账单的查询必须只能查询自己的,实现多用户的账单数据的隔离
        SysUser sysUser = validUser(token);
        Page page = houseService.getDeletedPageList(pageNum, pageSize, houseSearchVo, sysUser);
        stopWatch.stop();
        log.info("com.house.agents.controller.HouseController.getDeletedList运行时长是: " + stopWatch.prettyPrint());
        return R.ok().data("items", page);
    }

    @PreAuthorize("hasAnyAuthority('bnt.house.excelDownload')")
    @ApiOperation("房子信息导出为excel")
    @GetMapping("/export")
    @LogAnnotation
    // 这个地方不能有返回值,否则会覆盖服务器给前端的response响应
    public void download(HttpServletRequest request, HttpServletResponse response, @RequestHeader(required = false, name = "token") String token) {
        // Cat.logEvent("download","download");
        if (StringUtils.isBlank(token)) {
            token = CookieUtils.getCookieValue(request, "vue_admin_template_token");
        }
        SysUser sysUser = validUser(token);
        Long userId = sysUser.getId();

        houseService.exportHouses(response, userId);
    }

    @PreAuthorize("hasAnyAuthority('bnt.house.remove')")
    @ApiOperation("根据id下架或者上架房子")
    @DeleteMapping("/{id}")
    @LogAnnotation
    public R removeById(@PathVariable("id") String id, @RequestHeader("token") String token) {
        // Cat.logEvent("removeById","removeById");
        SysUser sysUser = validUser(token);
        Long userId = sysUser.getId();

        // 加个判断,如果修改的不是自己的账单的数据抛出异常
        House house = houseService.getById(id);
        if (house.getUserId() != userId && !isAdmin(sysUser)) {
            throw new BusinessException(ResponseEnum.NOT_YOUSELF_ACCOUNT);
        }
        // houseService.removeById(id);
        LambdaUpdateWrapper<House> updateWrapper = Wrappers.lambdaUpdate(House.class).eq(House::getId, id);
        if (house.getHouseStatus() == HouseStatusEnum.HOUSE_DOWN.getCode()) {
            // 如果房子是下架状态则修改为上架
            updateWrapper.set(House::getHouseStatus, HouseStatusEnum.HOUSE_UP.getCode());
        } else {
            // 如果房子是上架状态则修改为下架
            updateWrapper.set(House::getHouseStatus, HouseStatusEnum.HOUSE_DOWN.getCode());
        }
        houseService.update(updateWrapper);
        return R.ok();
    }

    private boolean isAdmin(SysUser sysUser) {
        // Cat.logEvent("isAdmin","isAdmin");
        List<SysRole> roleList = sysUser.getRoleList();
        if (CollectionUtils.isEmpty(roleList)) {
            // 如果roleList为空的话,那么说明是有问题的,需要抛出一场
            log.info(String.format("interfaceName = %s , methodName = %s , parameter = userId : %s", "HouseServiceImpl", "getPageList", sysUser.getId()));
            throw new BusinessException("roleList为空");
        }
        return roleList.stream().map(SysRole::getRoleCode).anyMatch(roleCode -> roleCode.equals("SYSTEM"));
    }

    @Deprecated
    @PreAuthorize("hasAnyAuthority('bnt.house.update')")
    @ApiOperation("重新上架房子")
    @PutMapping("/{houseId}")
    @LogAnnotation
    public R rePublishHouse(@PathVariable("houseId") String houseId, @RequestHeader("token") String token) {
        // Cat.logEvent("rePublishHouse","rePublishHouse");
        SysUser sysUser = validUser(token);
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
    @LogAnnotation
    public R updateById(@RequestBody House house, @RequestHeader("token") String token) {
        // Cat.logEvent("updateById","updateById");
        SysUser sysUser = validUser(token);
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
    @LogAnnotation
    public R save(@RequestBody House house, @RequestHeader("token") String token) {
        // Cat.logEvent("save","save");
        SysUser sysUser = validUser(token);
        Long userId = sysUser.getId();
        house.setUserId(userId);
        houseService.save(house);
        return R.ok();
    }

    @Deprecated
    @PreAuthorize("hasAnyAuthority('bnt.house.remove')")
    @ApiOperation("批量删除")
    @DeleteMapping()
    @LogAnnotation
    public R batchRemoveByIds(@RequestBody List<String> houseIds, @RequestHeader("token") String token) {
        // Cat.logEvent("batchRemoveByIds","batchRemoveByIds");
        SysUser sysUser = validUser(token);
        Long userId = sysUser.getId();
        // 加个判断,如果修改的不是自己房子的数据抛出异常
        boolean checkUser = houseIds.stream().map(houseId -> {
            House house = houseService.getById(houseId);
            return house.getUserId();
        }).allMatch(t -> t.equals(userId));
        if (!checkUser && !isAdmin(sysUser)) {
            throw new BusinessException(ResponseEnum.NOT_YOUSELF_ACCOUNT);
        }
        // houseService.removeByIds(houseIds);
        houseService.update(Wrappers.lambdaUpdate(House.class).in(House::getId, houseIds).set(House::getHouseStatus, HouseStatusEnum.HOUSE_DOWN.getCode()));
        return R.ok();
    }

    // @PreAuthorize("hasAnyAuthority('bnt.house.share')")
    @ApiOperation("批量查询分享的房子")
    @GetMapping("/shareHouse")
    @LogAnnotation
    public R batchGetShareHousesByShareId(@RequestParam("shareId") String shareId) {
        List<House> houses = shareEntityService.batchGetHousesByShareId(shareId);
        return R.ok().data("houses",houses);
    }

    @PreAuthorize("hasAnyAuthority('bnt.house.share')")
    @ApiOperation("批量分享")
    @PostMapping("/share")
    @LogAnnotation
    public R batchShareByIds(@RequestBody List<String> houseIds, @RequestHeader("token") String token) {
        // Cat.logEvent("batchRemoveByIds","batchRemoveByIds");
        if (CollectionUtils.isEmpty(houseIds)) {
            throw new BusinessException(ResponseEnum.SHARE_ERROR);
        }
        SysUser sysUser = validUser(token);
        Long userId = sysUser.getId();

        String shareId = shareEntityService.batchShareByIds(houseIds,userId);
        return R.ok().data("shareId",shareId);
    }

    @Deprecated
    @PreAuthorize("hasAnyAuthority('bnt.house.update')")
    @ApiOperation("批量重新上架")
    @PutMapping("/batch/republish")
    @LogAnnotation
    public R batchRepublishByIds(@RequestBody List<String> houseIds, @RequestHeader("token") String token) {
        // Cat.logEvent("batchRepublishByIds","batchRepublishByIds");
        SysUser sysUser = validUser(token);
        Long userId = sysUser.getId();
        // 加个判断,如果修改的不是自己房子的数据抛出异常
        boolean checkUser = houseIds.stream().map(houseId -> {
            House house = houseService.getByIdDeleted(Long.valueOf(houseId));
            return house.getUserId();
        }).allMatch(t -> t.equals(userId));
        if (!checkUser && !isAdmin(sysUser)) {
            throw new BusinessException(ResponseEnum.NOT_YOUSELF_ACCOUNT);
        }
        // houseService.rePublishByIds(houseIds);
        houseService.update(Wrappers.lambdaUpdate(House.class).in(House::getId, houseIds).set(House::getHouseStatus, HouseStatusEnum.HOUSE_UP.getCode()));
        return R.ok();
    }


    @Deprecated
    @PreAuthorize("hasAnyAuthority('bnt.house.list')")
    @ApiOperation(value = "获取地铁线路信息")
    @GetMapping("/subway")
    @LogAnnotation
    public R getSubway() {
        // Cat.logEvent("getSubway","getSubway");
        List<Subway> list = subwayService.list();
        return R.ok().data("items", list);
    }
}

