package com.house.agents.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class OssServiceImplTest {
    @Test
    public void test1(){
        String contentType = "video/mp4 file.getContentTyp";
        boolean video = StringUtils.contains(contentType,"video");
        System.out.println("video = " + video);
    }

}