package com.house.agents;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication
public class TestDemo231212 {
    @Test
    void test1(){
        Long data;
        data = (false ? 1L : (false ? 2L : null));
        System.out.println("data = " + data);
    }
}
