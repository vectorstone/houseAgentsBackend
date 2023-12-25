package com.house.agents;

import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class JasyptTest {
    @Autowired
    private StringEncryptor encryptor;
    @Test
    void test (){
        List<String> properties = new ArrayList<>();
        properties.add("root");
        properties.add("j45ku87R3i2i9v");
        properties.add("houseAgents231222");
        properties.forEach(property -> {
            System.out.println(property + " = " + encryptor.encrypt(property));
        });
        String houseAgents231222 = encryptor.encrypt("houseAgents231222");
        System.out.println("houseAgents231222 = " + houseAgents231222);
    }
}
