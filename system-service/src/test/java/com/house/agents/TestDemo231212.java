package com.house.agents;

import com.house.agents.entity.House;
import com.house.agents.mapper.HouseMapper;
import com.house.agents.service.HouseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;
import java.util.List;

// @SpringBootTest
public class TestDemo231212 {
    @Test
    void test1(){
        Long data;
        data = (false ? 1L : (false ? 2L : null));
        System.out.println("data = " + data);
    }

    @Test
    void test2(){
        Long a = 1735326701858136071L;
        Long b = 1735326701858136071L;
        boolean b1 = a == b;
        if (b1) {
            System.out.println("b = " + b);
        }
    }
    @Autowired
    HouseService houseService;
    @Resource
    HouseMapper houseMapper;
    @Test
    void test3(){
        // House deletedHouse = houseMapper.getDeletedHouse(1735326701858136066L);
        // System.out.println("deletedHouse = " + deletedHouse);
        // houseService.getById()
        // int totalCount = houseMapper.getTotalCount(1687030184168923138L);
        // System.out.println("totalCount = " + totalCount);
        // List<House> deletedHousesWithPage = houseMapper.getDeletedHousesWithPage(1687030184168923138L, 0, 10);
        // System.out.println("deletedHousesWithPage = " + deletedHousesWithPage);
        // int count = houseService.getCount(1687030184168923138L);
        // System.out.println("count = " + count);
        // List<House> deletedHousesWithPage = houseService.getDeletedHousesWithPage(1687030184168923138L, 1, 10);
        // System.out.println("deletedHousesWithPage = " + deletedHousesWithPage);
    }
    @Test
    void test4(){
        int page = 20 / 86 + (20 % 86 != 0 ? 1: 0);
        System.out.println("page = " + page);
    }

}
