package com.house.agents;

import com.house.agents.utils.MD5;
import org.junit.jupiter.api.Test;

/**
 * @Description:
 * @Author: Gavin
 * @Date: 8/4/2023 9:42 AM
 */
public class HashTableTest {
    @Test
    void test(){
        /* Map<String,String> hashTable = new Hashtable<>();
        for (int i = 0; i < 20; i++) {
            String num = i + "";
            new Thread(()->{
                hashTable.put(num, UUID.randomUUID().toString().replace("-","").substring(0,12));
                String s = hashTable.get(num).toString();
                System.out.println("s = " + s);
            }).start();
        } */
        String encrypt = MD5.encrypt("123456");
        System.out.println("encrypt = " + encrypt);
    }
}
