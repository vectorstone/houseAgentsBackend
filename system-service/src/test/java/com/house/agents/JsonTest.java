package com.house.agents;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.house.agents.entity.House;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonTest {
    @Test
    public void test1(){
        House house = new House();
        house.setId(1L);
        house.setCommunity("1L");
        HashMap<Long, House> houseHashMap = Maps.newHashMap();
        houseHashMap.put(1L,house);

        String jsonString = JSON.toJSONString(houseHashMap);
        System.out.println("jsonString = " + jsonString);

        Map<Long, House> map = JSON.parseObject(jsonString, Map.class);
        System.out.println("map = " + map);
        House house1 = map.get(1L);
        System.out.println("house1 = " + house1);

        // JSON.parseObject(jsonString, new TypeReference<List<Map<String, Object>>>() {});
        Map<Long, House> longHouseMap = JSON.parseObject(jsonString, new TypeReference<Map<Long, House>>() {});
        House house2 = longHouseMap.get(1L);
        System.out.println("house2 = " + house2);
    }
}
