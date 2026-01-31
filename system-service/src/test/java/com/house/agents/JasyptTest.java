package com.house.agents;

import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Disabled("测试环境禁用 Jasypt，跳过此测试")
public class JasyptTest {
    @Autowired
    private StringEncryptor encryptor;
    @Test
    void test (){
        List<String> properties = new ArrayList<>();
        properties.add("root");
        properties.add("");
        properties.add("");
        properties.forEach(property -> {
            System.out.println(property + " = " + encryptor.encrypt(property));
        });
        String houseAgents231222 = encryptor.encrypt("");
        System.out.println(" = " + houseAgents231222);
    }

    @Test
    void test2 (){
        List<String> properties = new ArrayList<>();
        properties.add("");
        properties.add("");
        properties.forEach(property -> {
            System.out.println(property + " = " + encryptor.encrypt(property));
        });

    }
}
