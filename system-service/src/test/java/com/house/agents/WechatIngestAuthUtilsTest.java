package com.house.agents;

import com.house.agents.config.WechatIngestProperties;
import com.house.agents.utils.WechatIngestAuthUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class WechatIngestAuthUtilsTest {

    @Test
    public void verifyShouldPassWithMatchingSignature() {
        WechatIngestProperties properties = new WechatIngestProperties();
        properties.setEnabled(true);
        properties.setTokenHash("collector-secret-hash");
        properties.setAllowedClockSkewSeconds(300);
        properties.setReplayTtlSeconds(60);

        InMemoryRedisTemplate redisTemplate = new InMemoryRedisTemplate();
        String body = "{\"sourceKey\":\"abc\"}";
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String idempotencyKey = "idem-1";
        String bodyHash = WechatIngestAuthUtils.sha256Hex(body.getBytes(StandardCharsets.UTF_8));
        String signature = WechatIngestAuthUtils.hmacSha256Hex(
                properties.getTokenHash(),
                "POST" + "/api/wechat-house-drafts/ingest" + timestamp + idempotencyKey + bodyHash
        );

        boolean verified = WechatIngestAuthUtils.verify(
                "POST",
                "/api/wechat-house-drafts/ingest",
                timestamp,
                idempotencyKey,
                signature,
                body,
                properties,
                redisTemplate
        );

        Assertions.assertTrue(verified);
    }

    @SuppressWarnings("unchecked")
    static class InMemoryRedisTemplate extends RedisTemplate<String, Object> {
        private final java.util.Map<String, Object> data = new java.util.HashMap<>();
        private final ValueOperations<String, Object> valueOperations;

        InMemoryRedisTemplate() {
            valueOperations = Mockito.mock(ValueOperations.class);
            Mockito.doAnswer(invocation -> {
                data.put(invocation.getArgument(0), invocation.getArgument(1));
                return null;
            }).when(valueOperations).set(Mockito.anyString(), Mockito.any(), Mockito.anyLong(), Mockito.any(TimeUnit.class));
        }

        @Override
        public Boolean hasKey(String key) {
            return data.containsKey(key);
        }

        @Override
        public ValueOperations<String, Object> opsForValue() {
            return valueOperations;
        }
    }
}
