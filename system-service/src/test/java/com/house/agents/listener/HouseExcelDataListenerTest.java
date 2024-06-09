package com.house.agents.listener;

import com.house.agents.entity.House;
import com.house.agents.entity.vo.HouseVo;
import org.junit.Test;
import org.springframework.beans.BeanUtils;

import static org.junit.Assert.*;

public class HouseExcelDataListenerTest {

    @Test
    public void test(){
        HouseVo houseVo = new HouseVo();
        houseVo.setId("1234567");
        House house = new House();
        //将dictVo的属性值设置给dict对象,将相同名称+相同类型的属性值,拷贝给另一个对象
        BeanUtils.copyProperties(houseVo,house);

        Long id = house.getId();
        System.out.println("id = " + id);
    }

}