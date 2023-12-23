package com.house.agents;

import com.house.agents.utils.MD5;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @Description:
 * @Author: Gavin
 * @Date: 9/27/2023 11:32 AM
 */
@SpringBootTest
public class PasswordMD5 {
    @Test
    void test1(){
        String encrypt = MD5.encrypt("123456");
        System.out.println("encrypt = " + encrypt);

    }
    @Autowired
    PasswordEncoder passwordEncoder;
    @Test
    void test2(){
        String encodePassword = passwordEncoder.encode("123456");
        System.out.println("encodePassword = " + encodePassword);
    }
}
