package com.house.agents.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.house.agents.Enum.FileContentTypeEnum;
import com.house.agents.Enum.HouseStatusEnum;
import com.house.agents.Enum.SearchFileTypeEnum;
import com.house.agents.Enum.SearchHouseStatusEnum;
import com.house.agents.entity.House;
import com.house.agents.entity.HouseAttachment;
import com.house.agents.entity.SysRole;
import com.house.agents.entity.SysUser;
import com.house.agents.entity.vo.HouseSearchVo;
import com.house.agents.entity.vo.HouseVo;
import com.house.agents.listener.HouseExcelDataListener;
import com.house.agents.mapper.HouseAttachmentMapper;
import com.house.agents.mapper.HouseMapper;
import com.house.agents.result.ResponseEnum;
import com.house.agents.service.HouseAttachmentService;
import com.house.agents.service.HouseService;
import com.house.agents.service.SysUserService;
import com.house.agents.utils.Asserts;
import com.house.agents.utils.BusinessException;
import com.house.agents.utils.FutureUtils;
import com.house.agents.utils.XMDLogFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Gavin
 * @since 2023-07-28
 */
@Service
@Slf4j
public class HouseServiceImpl extends ServiceImpl<HouseMapper, House> implements HouseService {
    @Autowired
    private HouseAttachmentService houseAttachmentService;

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private HouseMapper houseMapper;

    @Autowired
    private HouseAttachmentMapper houseAttachmentMapper;

    @Autowired
    private SysUserService sysUserService;

    @Override
    public List<HouseAttachment> getBannerList() {
        List<HouseAttachment> images = houseAttachmentService.list(Wrappers.lambdaQuery(HouseAttachment.class).eq(HouseAttachment::getContentType, FileContentTypeEnum.HOUSE_IMAGE).last("limit 100"));
        // 生成一个[a,b]范围的随机数的方式:(int)(Math.random() * (b - a + 1)+a)

        // Set<Integer> indices = Sets.newHashSet();
        // while (indices.size() <= 10) {
        //     // 随机的从查询出来的图片里面获取10个下标
        //     indices.add((int) (Math.random() * (99 + 1)));
        // }
        // List<HouseAttachment> sortedImages = Lists.newArrayList();
        // indices.forEach(index -> sortedImages.add(images.get(index)));
        // return sortedImages;
        Random random = new Random();
        Set<Integer> indices = IntStream.generate(() -> random.nextInt(100))
                .distinct()
                .limit(10)
                .boxed()
                .collect(Collectors.toSet());

        return indices.stream()
                .map(images::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<House> getHouseInfoNoLogin() {
        List<House> houses = this.list(Wrappers.lambdaQuery(House.class).last("limit 0,8").orderByDesc(House::getUpdateTime));
        setHouseAttachment(houses, null);
        return houses;
    }

    @Override
    public void importHouses(MultipartFile file, Long userId) {
        // 校验判断文件是否合规(文件必须存在,后缀,文件的大小)
        // 判断文件的后缀是否合规
        boolean flag = file.getOriginalFilename().toLowerCase().endsWith(".xls") ||
                file.getOriginalFilename().toLowerCase().endsWith(".xlsx") ||
                file.getOriginalFilename().toLowerCase().endsWith(".cvs");
        Asserts.AssertTrue(flag, ResponseEnum.UPLOAD_ERROR);
        // 判断文件的大小不可以为0 断言文件的大小必须 > 0 ,如果小于0,立马抛异常
        Asserts.AssertTrue(file.getSize() > 0, ResponseEnum.UPLOAD_ERROR);
        // 判断文件必须存在
        Asserts.AssertNotNull(file, ResponseEnum.DATA_NULL_ERROR);

        // 文件上传的核心业务代码
        try {
            // 使用MultipartFile的输入流来读取文件
            EasyExcel.read(file.getInputStream())
                    .head(HouseVo.class)
                    .sheet(0)
                    .registerReadListener(new HouseExcelDataListener(this, userId))
                    .doRead();
        } catch (Exception e) { // 放大异常的类型
            // 将异常的信息记录到日志文件中
            log.error("出异常了,异常信息为:{}" + ExceptionUtils.getStackTrace(e));
            // 抛出我们自定义的异常
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR);
        }
    }

    private void setSearchVoRent(HouseSearchVo houseSearchVo) {
        if (StringUtils.isNotEmpty(houseSearchVo.getRentRange())) {
            // 1000-1500元
            String coarseRentRange = houseSearchVo.getRentRange().replaceAll("元", "");
            String[] split = coarseRentRange.split("-");
            houseSearchVo.setStartRent(new BigDecimal(split[0]));
            houseSearchVo.setEndRent(new BigDecimal(split[1]));
        }
    }

    @Override
    public Page getPageList(Integer pageNum, Integer pageSize, HouseSearchVo houseSearchVo, SysUser sysUser) {
        Page<House> housePage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<House> wrapper = null;
        Long userId = sysUser.getId();
        // if (isAdmin(sysUser)) {
        //     wrapper = Wrappers.lambdaQuery(House.class);
        // } else {
        //     wrapper = Wrappers.lambdaQuery(House.class).eq(House::getUserId, userId);
        // }
        // 允许普通的用户查询所有的数据,但是房东的信息会暴露
        wrapper = Wrappers.lambdaQuery(House.class);
        // 构建查询的条件
        if (houseSearchVo != null) {
            // 小区名称
            String houseId = houseSearchVo.getId();
            String community = houseSearchVo.getCommunity();
            String subway = houseSearchVo.getSubway();
            String roomNumber = houseSearchVo.getRoomNumber();
            String orientation = houseSearchVo.getOrientation();
            String keyOrPassword = houseSearchVo.getKeyOrPassword();
            String remark = houseSearchVo.getRemark();
            String landlordName = houseSearchVo.getLandlordName();
            int fileType = houseSearchVo.getFileType();
            int houseStatus = houseSearchVo.getHouseStatus();
            List<String> subways = houseSearchVo.getSubways();

            // if (StringUtils.isNotEmpty(landlordName)){

            //     List<SysUser> landlords = sysUserService.list(Wrappers.lambdaQuery(SysUser.class).like(SysUser::getName, landlordName));
            //     if (CollectionUtils.isNotEmpty(landlords)){
            //         // 根据房东的姓名查询出来对应的userId
            //         List<Long> userIds = landlords.stream().map(SysUser::getId).collect(Collectors.toList());
            //         wrapper.in(House::getUserId,userIds);
            //     }
            // }
            if (StringUtils.isNotEmpty(houseId)) wrapper.eq(House::getId, houseId);

            if (StringUtils.isNotEmpty(landlordName)) wrapper.like(House::getLandlordName, landlordName);

            // 模糊
            if (StringUtils.isNotEmpty(community)) wrapper.like(House::getCommunity, community);

            if (StringUtils.isNotEmpty(subway)) wrapper.like(House::getSubway, subway);

            if (CollectionUtils.isNotEmpty(subways)) {

                // Preparing: SELECT id,user_id,community,subway,room_number,rent,orientation,keyOrPassword,remark,create_time,update_time,is_deleted AS deleted,houseStatus,landlordName FROM house WHERE is_deleted=0 AND ((subway LIKE ? OR subway LIKE ?)) ORDER BY update_time DESC LIMIT ?
                // ==> Parameters: %巨峰路%(String), %杨高北路%(String), 10(Long)
                wrapper.and(w -> Sets.newHashSet(subways).forEach(item -> w.or().like(House::getSubway, item)));
            }

            if (StringUtils.isNotEmpty(roomNumber)) wrapper.like(House::getRoomNumber, roomNumber);

            setSearchVoRent(houseSearchVo);
            BigDecimal startRent = houseSearchVo.getStartRent();
            BigDecimal endRent = houseSearchVo.getEndRent();
            // amount金额 >
            if (startRent != null && startRent.compareTo(new BigDecimal(0)) > 0) {
                wrapper.ge(House::getRent, startRent);
            }

            if (endRent != null && endRent.compareTo(new BigDecimal(0)) > 0) {
                wrapper.le(House::getRent, endRent);
            }

            if (StringUtils.isNotEmpty(orientation)) {
                wrapper.like(House::getOrientation, orientation);
            }

            if (StringUtils.isNotEmpty(keyOrPassword)) {
                wrapper.like(House::getKeyOrPassword, keyOrPassword);
            }

            if (StringUtils.isNotEmpty(remark)) {
                wrapper.like(House::getRemark, remark);
            }

            // createTime 介于某一范围
            if (houseSearchVo.getStartTime() != null && houseSearchVo.getEndTime() != null) {
                // 搜索的其实日期和结束日期不能为空
                wrapper.le(House::getCreateTime, houseSearchVo.getEndTime()).ge(House::getCreateTime, houseSearchVo.getStartTime());
            }
            // 根据附件的附件状态来查询相关的数据
            if (fileType != SearchFileTypeEnum.DEFAULT.getCode()) {
                List<Long> houseIds = houseAttachmentMapper.getHouseId();
                if (fileType == SearchFileTypeEnum.NOT_EMPTY.getCode()) {
                    wrapper.in(House::getId, houseIds);
                } else if (fileType == SearchFileTypeEnum.EMPTY.getCode()) {
                    wrapper.notIn(House::getId, houseIds);
                }
            }
            // 根据房子的状态来查询对应的数据
            // 这个地方有个坑,数据库里面房子的状态0是上架,1是下架,前端里面传值的时候,如果不设置值,默认是0
            // 所以前端传过来的房子状态的字段必须进行转换
            if (houseStatus != SearchHouseStatusEnum.DEFAULT.getCode()) {
                if (houseStatus == SearchHouseStatusEnum.HOUSE_UP.getCode()) {
                    wrapper.eq(House::getHouseStatus, HouseStatusEnum.HOUSE_UP.getCode());
                } else if (houseStatus == SearchHouseStatusEnum.HOUSE_DOWN.getCode()) {
                    wrapper.eq(House::getHouseStatus, HouseStatusEnum.HOUSE_DOWN.getCode());
                }
            }
        }
        // wrapper.orderByDesc(House::getCreateTime);
        wrapper.orderByDesc(House::getUpdateTime); // 将最新修改的房子置顶在前面
        Page<House> housePageData = this.page(housePage, wrapper);
        // 设置house的附件和房东的姓名的方法
        setExtraAttributes(housePageData, sysUser);

        return housePageData;
    }

    private boolean isAdmin(SysUser sysUser) {
        if (Objects.isNull(sysUser)) return false;
        List<SysRole> roleList = sysUser.getRoleList();
        if (CollectionUtils.isEmpty(roleList)) {
            // 如果roleList为空的话,那么说明是有问题的,需要抛出一场
            log.info(String.format("interfaceName = %s , methodName = %s , parameter = userId : %s", "HouseServiceImpl", "getPageList", sysUser.getId()));
            throw new BusinessException("roleList为空");
        }
        // 只要当前该用户拥有的角色列表里面有任何一个角色的roleCode和SYSTEM相等,说明该用户具有管理员的权限,那么就默认查询所有的数据
        return roleList.stream().map(SysRole::getRoleCode).anyMatch(roleCode -> roleCode.equals("SYSTEM"));
    }


    /**
     * 该方法用来设置house额外的附件的信息以及房东姓名的属性
     *
     * @param housePageData 分页的page对象
     */
    private void setExtraAttributes(Page<House> housePageData, SysUser sysUser) {
        // sysUser前面已经校验过了,不可能为空了
        if (housePageData != null && CollectionUtils.isNotEmpty(housePageData.getRecords())) {
            // 如果查询出来的结果不为空的话,那么就设置对应的房子的附件进去
            List<House> houses = housePageData.getRecords();
            setHouseAttachment(houses, sysUser);
            // if (fileType == SearchFileTypeEnum.DEFAULT.getCode()) {
            //     return;
            // }
            // if (fileType == SearchFileTypeEnum.EMPTY.getCode()) {
            //     houses = houses.stream().filter(house -> CollectionUtils.isEmpty(house.getHouseAttachment())).collect(Collectors.toList());
            // }
            // if (fileType == SearchFileTypeEnum.VIDEO.getCode()) {
            //     houses = houses.stream().filter(house -> {
            //         List<HouseAttachment> houseAttachment = house.getHouseAttachment();
            //         return CollectionUtils.isNotEmpty(houseAttachment) && houseAttachment.stream().allMatch(attach -> attach.getContentType() == FileContentTypeEnum.HOUSE_VIDEO.getCode());
            //     }).collect(Collectors.toList());
            // }
            // if (fileType == SearchFileTypeEnum.IMAGE.getCode()) {
            //     houses = houses.stream().filter(house -> {
            //         List<HouseAttachment> houseAttachment = house.getHouseAttachment();
            //         return CollectionUtils.isNotEmpty(houseAttachment) && houseAttachment.stream().allMatch(attach -> attach.getContentType() == FileContentTypeEnum.HOUSE_IMAGE.getCode());
            //     }).collect(Collectors.toList());
            // }
            // if (fileType == SearchFileTypeEnum.ALL.getCode()) {
            //     houses = houses.stream().filter(house -> {
            //         List<HouseAttachment> houseAttachment = house.getHouseAttachment();
            //         return CollectionUtils.isNotEmpty(houseAttachment) && (houseAttachment.stream().anyMatch(attach -> attach.getContentType() == FileContentTypeEnum.HOUSE_IMAGE.getCode()) &&
            //                 houseAttachment.stream().anyMatch(attach -> attach.getContentType() == FileContentTypeEnum.HOUSE_VIDEO.getCode()));
            //     }).collect(Collectors.toList());
            // }
            // housePageData.setRecords(houses);
        }
    }

    @Override
    public void setHouseAttachment(List<House> houses, SysUser sysUser) {
        HashMap<Long, CompletableFuture<List<HouseAttachment>>> houseAttachmentCfMap = Maps.newHashMap();
        // HashMap<Long, CompletableFuture<SysUser>> sysUserCfMap = Maps.newHashMap();

        houses.forEach(house -> {
            try {

                // 如果不是管理员的话, 那么就需要将房子信息里面的房东信息去除
                if (!isAdmin(sysUser)) {
                    house.setLandlordName("");
                }

                CompletableFuture<List<HouseAttachment>> houseAttachmentCf = getAttachmentCf(house.getId());
                houseAttachmentCfMap.put(house.getId(), houseAttachmentCf);

            } catch (Exception e) {
                log.info(XMDLogFormat.build().putTag("interfaceName", "setExtraAttributes").message(e.getMessage()));
                throw new RuntimeException(e);
            }
        });

        houses.forEach(house -> {
            Long houseId = house.getId();
            try {
                house.setHouseAttachment(FutureUtils.get(houseAttachmentCfMap.get(houseId)));
                // 设置图片的首图
                List<HouseAttachment> houseAttachments = house.getHouseAttachment();
                Optional.ofNullable(houseAttachments)
                        .orElse(Collections.emptyList())
                        .stream()
                        .filter(img -> img.getContentType() == FileContentTypeEnum.HOUSE_IMAGE.getCode())
                        .findFirst()
                        .ifPresent(img -> house.setHeadImage(img.getUrl()));


            } catch (Exception e) {
                log.info(XMDLogFormat.build().putTag("interfaceName", "setExtraAttributes").message(e.getMessage()));
                throw new RuntimeException(e);
            }
        });
    }

    private CompletableFuture<List<HouseAttachment>> getAttachmentCf(Long houseId) {
        LambdaQueryWrapper<HouseAttachment> queryWrapper = Wrappers.lambdaQuery(HouseAttachment.class).eq(HouseAttachment::getHouseId, houseId).orderByAsc(HouseAttachment::getContentType);
        // // 设置查询的条件,根据searchVo里面的fileType
        // if (fileType == SearchFileTypeEnum.IMAGE.getCode()) {
        //     queryWrapper.and(t -> t.eq(HouseAttachment::getContentType, FileContentTypeEnum.HOUSE_IMAGE));
        // } else if (fileType == SearchFileTypeEnum.VIDEO.getCode()) {
        //     queryWrapper.and(t -> t.eq(HouseAttachment::getContentType, FileContentTypeEnum.HOUSE_VIDEO));
        // } else {
        //
        // }
        // 查询房子所属的附件并设置进去
        CompletableFuture<List<HouseAttachment>> houseAttachmentCf = CompletableFuture.supplyAsync(() -> houseAttachmentService.list(
                queryWrapper), executorService);
        return houseAttachmentCf;
    }

    /**
     * 重要: userId和userIds这两个条件不可以同时存在,不然的话sql语句会报错
     *
     * @param pageNum
     * @param pageSize
     * @param houserSearchVo
     * @param sysUser
     * @return
     */
    @Override
    public Page getDeletedPageList(Integer pageNum, Integer pageSize, HouseSearchVo houserSearchVo, SysUser sysUser) {
        Long userId = sysUser.getId();

        // 判断是否为管理员,如果是管理员的话,那么就将userId设置为0,这样sql里面查询的时候就会查询所有的数据
        List<Long> userIds = Lists.newArrayList();
        if (isAdmin(sysUser)) {
            userId = 0L;
            // 只有是管理员的情况下,才能根据房东的姓名进行筛选对应的房源信息,普通的用户只能查看到自己的房源数据,无法筛选其他人的数据
            String landlordName = Optional.ofNullable(houserSearchVo).map(HouseSearchVo::getLandlordName).orElse("");
            // String landlordName = houserSearchVo.getLandlordName();
            if (StringUtils.isNotEmpty(landlordName)) {
                List<SysUser> landlords = sysUserService.list(Wrappers.lambdaQuery(SysUser.class).like(SysUser::getName, landlordName));
                userIds = Optional.ofNullable(landlords).orElse(Lists.newArrayList()).stream().map(SysUser::getId).collect(Collectors.toList());
                // if (CollectionUtils.isNotEmpty(landlords)){
                //     // 根据房东的姓名查询出来对应的userId
                //     userIds = landlords.stream().map(SysUser::getId).collect(Collectors.toList());
                // }
            }
        }

        Page<House> housePage = new Page<>(pageNum, pageSize);
        // LambdaQueryWrapper<House> wrapper = Wrappers.lambdaQuery(House.class).eq(House::getUserId, userId).eq(House::getDeleted,1);
        // Page<House> housePageData = this.page(housePage, wrapper);
        // 自己动手做分页
        // 查询总的记录的数量  SELECT COUNT(*) FROM house WHERE is_deleted = 0 AND (user_id = ?)
        Integer totalCount = baseMapper.getTotalCount(userId);
        // 计算总的页数的大小
        int pages = totalCount / pageSize + (totalCount % pageSize != 0 ? 1 : 0);
        // 判断当前的页数是否超出了范围,如果超出了范围,那么就默认按照第一页的数据来查询
        if (pageNum <= 0 || pageNum > pages) {
            pageNum = 1;
        }
        // 查询对应的数据 (pageNum - 1) * pageSize   limit (pageNum - 1) * pageSize , pageSize
        // SELECT id,user_id,community,subway,room_number,rent,orientation,keyOrPassword,remark,create_time,update_time,is_deleted AS deleted FROM house WHERE is_deleted=0 AND (user_id = ?) ORDER BY create_time DESC LIMIT ?,?
        int limitNum = (pageNum - 1) * pageSize;


        List<House> deletedHousesWithPage = null;
        try {
            deletedHousesWithPage = baseMapper.getDeletedHousesWithPage(userId, limitNum, pageSize, userIds);
        } catch (Exception e) {
            log.info(XMDLogFormat.build().putTag("interfaceName", "getDeletedHousesWithPage").message(String.valueOf("查询已下架房源失败,userId:" + String.valueOf(userId))));
            throw new BusinessException(String.valueOf(e));
        }

        // 设置到page对象里面然后返回数据
        housePage.setTotal(totalCount);
        housePage.setSize(pageSize);
        housePage.setCurrent(pageNum);
        housePage.setOptimizeCountSql(true);
        housePage.setHitCount(false);
        housePage.setSearchCount(true);
        housePage.setPages(pages);
        housePage.setRecords(deletedHousesWithPage);
        setExtraAttributes(housePage, sysUser);
        return housePage;
    }


    @Override
    public void exportHouses(HttpServletResponse response, Long userId) {
        // 1.先查询数据库中的账单件,然后将其转为BookVo类型的对象
        // 下面这个方法获取到的house的isDeleted为0,并不是所有的数据,还需要查询出所有的数据
        List<HouseVo> houseVos = this.list(Wrappers.lambdaQuery(House.class).eq(House::getUserId, userId)).stream().map(house -> {
            HouseVo houseVo = new HouseVo();
            // 使用工具类,将查询出来的对象的属性转换为Dict类型的对象
            BeanUtils.copyProperties(house, houseVo);
            houseVo.setId(String.valueOf(house.getId()));
            houseVo.setUserId(String.valueOf(house.getUserId()));
            return houseVo;
        }).collect(Collectors.toList());

        // 查询所有下架的房子,然后添加到house的集合里面
        //  SysUser sysUser = sysUserService.getById(userId);
        //  int pageNum = 1;
        //  List<Page> pageList = Lists.newArrayList();
        //  while (true) {
        //
        //      Page deletedPageList = this.getDeletedPageList(pageNum, 30, null, sysUser);
        //      pageList.add(deletedPageList);
        //      pageNum ++;
        //  }

        // 在本地的时候,我们可以将数据的集合写入到一个excel文件中
        // 但是通过浏览器的下载,我们需要将数据集合写入到一个内存中的excel文件中再通过输出流写个浏览器

        try {
            // 配置响应头,告诉浏览器应该如何解析响应体中的数据流
            response.setHeader("content-disposition", "attachment;filename=account" +
                    new DateTime().toString("yyyyMMdd") + ExcelTypeEnum.XLSX.getValue());

            // 将字典文件集合以流的方式写入到响应体中
            EasyExcel.write(response.getOutputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .head(HouseVo.class)
                    .sheet(0)
                    .doWrite(houseVos);
        } catch (Exception e) {
            log.error("待租房文件下载异常,异常信息为:{}" + e.getStackTrace());
            throw new BusinessException(ResponseEnum.EXPORT_DATA_ERROR);
        }
    }

    @Override
    public House getByIdDeleted(Long houseId) {
        // mybatis只会查询对应的is_deleted = 1 的数据,所以这个地方需要自定义sql来实现
        // return this.getOne(Wrappers.lambdaQuery(House.class).eq(House::getDeleted, 1).eq(House::getId, houseId));
        House deletedHouse = houseMapper.getDeletedHouse(houseId);
        return deletedHouse;
    }

    @Override
    public void rePublishById(Long houseId) {
        int i = houseMapper.rePublishDeletedHouse(houseId);
        if (i <= 0) {
            throw new BusinessException(ResponseEnum.REPUBLISH_HOUSE_ERROR);
        }
    }

    @Override
    public void rePublishByIds(List<String> houseIds) {
        int i = houseMapper.rePublishDeletedHouses(houseIds);
        if (i <= 0) {
            throw new BusinessException(ResponseEnum.REPUBLISH_HOUSE_ERROR);
        }
    }

    @Override
    public House getHouseInfo(String houseId) {
        CompletableFuture<House> houseCf = CompletableFuture.supplyAsync(() -> this.getById(houseId), executorService);
        CompletableFuture<List<HouseAttachment>> attachmentCf = getAttachmentCf(Long.valueOf(houseId));
        CompletableFuture<House> completeHouseFuture = houseCf.thenCombine(attachmentCf, (house, attachements) -> {
            if (!CollectionUtils.isEmpty(attachements)) {
                house.setHouseAttachment(attachements);
            }
            return house;
        });
        return FutureUtils.get(completeHouseFuture);
    }
}
