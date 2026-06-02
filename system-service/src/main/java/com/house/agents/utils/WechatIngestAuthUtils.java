package com.house.agents.utils;

import com.house.agents.config.WechatIngestProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class WechatIngestAuthUtils {

    private WechatIngestAuthUtils() {
    }

    public static boolean verify(
            String method,
            String path,
            String timestamp,
            String idempotencyKey,
            String signature,
            String body,
            WechatIngestProperties properties,
            RedisTemplate redisTemplate
    ) {
        if (!properties.isEnabled()) {
            return false;
        }
        if (StringUtils.isAnyBlank(timestamp, idempotencyKey, signature, properties.getTokenHash())) {
            return false;
        }
        long requestTs;
        try {
            requestTs = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            return false;
        }
        long now = Instant.now().getEpochSecond();
        if (Math.abs(now - requestTs) > properties.getAllowedClockSkewSeconds()) {
            return false;
        }
        String replayKey = "wechat:ingest:replay:" + idempotencyKey;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(replayKey))) {
            return false;
        }
        String bodyHash = sha256Hex(StringUtils.defaultString(body).getBytes(StandardCharsets.UTF_8));
        String expected = hmacSha256Hex(
                properties.getTokenHash(),
                method + path + timestamp + idempotencyKey + bodyHash
        );
        if (!StringUtils.equals(expected, signature)) {
            return false;
        }
        redisTemplate.opsForValue().set(replayKey, "1", properties.getReplayTtlSeconds(), TimeUnit.SECONDS);
        return true;
    }

    public static String sha256Hex(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(input);
            return toHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    public static String hmacSha256Hex(String secret, String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return toHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("HmacSHA256 signing failed", e);
        }
    }

    private static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
