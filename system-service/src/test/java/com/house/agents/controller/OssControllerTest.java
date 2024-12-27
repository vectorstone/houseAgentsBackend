package com.house.agents.controller;

import com.alibaba.fastjson.JSON;
import com.house.agents.entity.HouseAttachment;
import com.house.agents.utils.HttpClientUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

import static org.junit.Assert.*;

public class OssControllerTest {

    @Test
    public void test() throws IOException, ParseException {
        // String filePath = "http://tmp/3ZzHODGkkmTTbc694d3a6f684c0ef95e13e26c1392e7.jpeg";
        String filePath = "https://house-agents.oss-cn-beijing.aliyuncs.com/1738414200457338882/2023/12/23/1703322613373_f4f319.jpg";
        // File file = new File(filePath);
        // long totalSpace = file.getTotalSpace();
        // System.out.println("totalSpace = " + totalSpace);

        HttpClientUtils utils1 = new HttpClientUtils(filePath);
        utils1.get();
        String content = utils1.getContent();
        // 2.5将响应转为map集合
        Map<String,Object> map = JSON.parseObject(content, Map.class);
        System.out.println("map = " + map);
    }

    @Test
    public void test1() throws IOException, ParseException {
        System.out.println("true = " + true);
        HouseAttachment houseAttachment = new HouseAttachment();
        houseAttachment.setId(123L);
        Long id = houseAttachment.getId();
        System.out.println("id = " + id);
    }

}