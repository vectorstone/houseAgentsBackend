package com.house.agents.service.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.house.agents.entity.SysUser;
import com.house.agents.entity.UserLoginRecord;
import com.house.agents.mapper.UserLoginRecordMapper;
import com.house.agents.service.UserLoginRecordService;
import com.house.agents.utils.IpUtils;
import org.apache.velocity.util.ArrayListWrapper;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 * 用户登录记录表 服务实现类
 * </p>
 *
 * @author Atguigu
 * @since 2023-06-30
 */
@Service
public class UserLoginRecordServiceImpl extends ServiceImpl<UserLoginRecordMapper, UserLoginRecord> implements UserLoginRecordService {

    @Override
    public void saveLoginRecord(SysUser userInfo, HttpServletRequest request) {
        // 需要保存登录用户的id和登录用户的真实ip
        UserLoginRecord userLoginRecord = new UserLoginRecord();
        userLoginRecord.setUserId(userInfo.getId());
        userLoginRecord.setIp(IpUtils.getIpAddressAtService(request));
        this.save(userLoginRecord);
    }

    @Override
    public Map<String, Object> loginAndRegistryCount(String start, String end) {
        // 返回值里面需要有4个list集合
        // 集合1 : loginDate
        List<Map<String,Object>> loginDatas = baseMapper.getLoginRecords(start,end);
        // 遍历集合,将集合中的日期和数量取出来,变成两个新的list
        List<Date> loginDate = new ArrayList<>();
        // 集合2 : loginCounts
        List<String> loginCounts = new ArrayList<>();
        loginDatas.forEach(loginData -> {
            loginDate.add((Date)loginData.get("loginDate"));
            loginCounts.add(loginData.get("counts").toString());
        });

        // 集合3 : registryDate
        Map<String ,Integer> registMap = new HashMap<>();
        registMap.put("2023-07-10",2);
        registMap.put("2023-07-13",10);
        registMap.put("2023-07-18",50);
        registMap.put("2023-07-20",3);
        // 集合4 : registryCounts
        ArrayList<String> registDates = new ArrayList<>(registMap.keySet());
        ArrayList<Integer> registCounts = new ArrayList<>(registMap.values());

        Map<String,List<Object>> data = new HashMap<>();

        return null;
    }
}