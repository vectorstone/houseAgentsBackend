package com.house.agents.utils;

import lombok.extern.slf4j.Slf4j;


import java.util.concurrent.CompletableFuture;

@Slf4j
public class FutureUtils {
    public static <T> T get(CompletableFuture<T> cf) {
        try {
            if (cf == null) {
                return null;
            }
            return cf.get();
        }  catch (Exception e) {
            log.error("get result failed", e);
        }
        return null;
    }
}
