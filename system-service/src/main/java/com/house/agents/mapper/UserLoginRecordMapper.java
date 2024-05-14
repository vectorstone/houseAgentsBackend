package com.house.agents.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.house.agents.entity.UserLoginRecord;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface UserLoginRecordMapper extends BaseMapper<UserLoginRecord> {


    List<Map<String,Object>> getLoginRecords(@Param("start")String start, @Param("end")String end);
}
