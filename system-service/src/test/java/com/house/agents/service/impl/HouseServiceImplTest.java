package com.house.agents.service.impl;

import org.junit.Test;

import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class HouseServiceImplTest {
    @Test
    public void test() {
        Random random = new Random();
        Set<Integer> indices = IntStream.generate(() -> random.nextInt(100))
                .distinct()
                .limit(10)
                .boxed()
                .collect(Collectors.toSet());
        System.out.println("indices = " + indices);
    }

}