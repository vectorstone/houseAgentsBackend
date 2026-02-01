package com.house.agents;

import lombok.Data;

@Data
public class LombokTest {
    private String name;
    private Integer age;

    public static void main(String[] args) {
        LombokTest test = new LombokTest();
        test.setName("Test");
        test.setAge(25);
        System.out.println("Name: " + test.getName() + ", Age: " + test.getAge());
    }
}
