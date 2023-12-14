package com.house.agents.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.house.agents.entity.House;
import com.house.agents.entity.vo.HouseSearchVo;
import com.house.agents.entity.vo.HouseVo;
import com.house.agents.listener.HouseExcelDataListener;
import com.house.agents.mapper.HouseMapper;
import com.house.agents.result.ResponseEnum;
import com.house.agents.service.HouseService;
import com.house.agents.utils.Asserts;
import com.house.agents.utils.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Gavin
 * @since 2023-07-28
 */
@Service
public class HouseServiceImpl extends ServiceImpl<HouseMapper, House> implements HouseService {
    //账单文件的上传功能
    @Override
    public void importHouses(MultipartFile file, Long userId) {
        //校验判断文件是否合规(文件必须存在,后缀,文件的大小)
        //判断文件的后缀是否合规
        boolean flag =  file.getOriginalFilename().toLowerCase().endsWith(".xls") ||
                file.getOriginalFilename().toLowerCase().endsWith(".xlsx") ||
                file.getOriginalFilename().toLowerCase().endsWith(".cvs");
        Asserts.AssertTrue( flag,ResponseEnum.UPLOAD_ERROR );
        //判断文件的大小不可以为0 断言文件的大小必须 > 0 ,如果小于0,立马抛异常
        Asserts.AssertTrue(file.getSize() > 0, ResponseEnum.UPLOAD_ERROR);
        //判断文件必须存在
        Asserts.AssertNotNull(file,ResponseEnum.DATA_NULL_ERROR);


        //文件上传的核心业务代码
        try {
            //使用MultipartFile的输入流来读取文件
            EasyExcel.read(file.getInputStream())
                    .head(HouseVo.class)
                    .sheet(0)
                    .registerReadListener(new HouseExcelDataListener(this,userId))
                    .doRead();
        } catch (Exception e) { //放大异常的类型
            //将异常的信息记录到日志文件中
            log.error("出异常了,异常信息为:{}"+ ExceptionUtils.getStackTrace(e));
            //抛出我们自定义的异常
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR);
        }
    }

    @Override
    public Page getPageList(Integer pageNum, Integer pageSize, HouseSearchVo houserSearchVo, Long userId) {
        Page<House> housePage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<House> wrapper = Wrappers.lambdaQuery(House.class).eq(House::getUserId,userId);
        //构建查询的条件
        if(houserSearchVo != null){
            //amount金额 >
            if(houserSearchVo.getRent() != null && houserSearchVo.getRent().compareTo(new BigDecimal(0)) > 0){
                //搜索的金额不能为空,也不能为0,才能进来这里面
                wrapper.ge(House::getRent, houserSearchVo.getRent());
            }
            //createTime 介于某一范围
            if(houserSearchVo.getStartTime() != null && houserSearchVo.getEndTime() != null){
                //搜索的其实日期和结束日期不能为空
                wrapper.le(House::getCreateTime, houserSearchVo.getEndTime()).ge(House::getCreateTime, houserSearchVo.getStartTime());
            }
            //label标签(支付方式) 特定的几个类别
            if(StringUtils.isNotEmpty(houserSearchVo.getLabel())){
                wrapper.eq(House::getLabel, houserSearchVo.getLabel());
            }
            //remark备注 模糊查询
            if(StringUtils.isNotEmpty(houserSearchVo.getRemark())){
                wrapper.like(House::getRemark, houserSearchVo.getRemark());
            }
            //category类别 特定的几个大类
            if(StringUtils.isNotEmpty(houserSearchVo.getCategory())){
                wrapper.eq(House::getCategory, houserSearchVo.getCategory());
            }
            //subcategory子类 特定的几个小类别
            if(StringUtils.isNotEmpty(houserSearchVo.getSubcategory())){
                wrapper.eq(House::getSubcategory, houserSearchVo.getSubcategory());
            }
        }
        wrapper.orderByDesc(House::getCreateTime);
        return this.page(housePage, wrapper);
    }

//    @Override
//    public void exportAccount(HttpServletResponse response, Long userId) {
//        // 1.先查询数据库中的账单件,然后将其转为BookVo类型的对象
//        List<BookVo> bookVos = this.list(Wrappers.lambdaQuery(Book.class).eq(Book::getUser,userId)).stream().map(book -> {
//            BookVo bookVo = new BookVo();
//            // 使用工具类,将查询出来的对象的属性转换为Dict类型的对象
//            BeanUtils.copyProperties(book, bookVo);
//            return bookVo;
//        }).collect(Collectors.toList());
//        // 在本地的时候,我们可以将数据的集合写入到一个excel文件中
//        // 但是通过浏览器的下载,我们需要将数据集合写入到一个内存中的excel文件中再通过输出流写个浏览器
//
//        try {
//            // 配置响应头,告诉浏览器应该如何解析响应体中的数据流
//            response.setHeader("content-disposition", "attachment;filename=account" +
//                    new DateTime().toString("yyyyMMdd") + ExcelTypeEnum.XLSX.getValue());
//
//            // 将字典文件集合以流的方式写入到响应体中
//            EasyExcel.write(response.getOutputStream())
//                    .excelType(ExcelTypeEnum.XLSX)
//                    .head(BookVo.class)
//                    .sheet(0)
//                    .doWrite(bookVos);
//        } catch (Exception e) {
//            log.error("账单文件下载异常,异常信息为:{}" + e.getStackTrace());
//            throw new BusinessException(ResponseEnum.EXPORT_DATA_ERROR);
//        }
//    }
//
//    //获取按月份统计的每月支出汇总
//    @Override
//    public Map<String, List<String>> getMonthlyCost(String start, String end, Long userId) {
//        //start和end格式 '2023-07'
//        List<MonthCostVo> monthCostVoList = baseMapper.getMonthlyCost(start,end,userId);
//        //要将这个list转成两个list,一个是月份的list,另外一个是money的list
//        //准备数据
//        Map<String,List<String>> map = new HashMap<>();
//        List<String> month = new ArrayList<>();
//        List<String> money = new ArrayList<>();
//        monthCostVoList.forEach(mm -> {
//            month.add(mm.getCdate());
//            money.add(mm.getCost().toString());
//        });
//        map.put("month",month);
//        map.put("money",money);
//        return map;
//    }
//
//    @Override
//    public Map<String, List<String>> getLargeAreaData(Long userId) {
//        //准备所需要的map和list集合
//        Map<String,List<String>> map = new HashMap<>();
//        List<String> day = new ArrayList<>();
//        List<String> money = new ArrayList<>();
//        //查询获取所有的数据
//        List<Book> list = this.list(Wrappers.lambdaQuery(Book.class).eq(Book::getUser,userId).orderByAsc(Book::getCreateTime));
//        list.forEach(ledger -> {
//            //时间需要处理一下
//            Date createTime = ledger.getCreateTime();
//            DateTime dateTime = new DateTime(createTime);
//            String date = dateTime.toString("yyyy-MM-dd");
//            day.add(date);
//            money.add(ledger.getAmount().toString());
//        });
//        map.put("day",day);
//        map.put("money",money);
//        return map;
//    }
}
