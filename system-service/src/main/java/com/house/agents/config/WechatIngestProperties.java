package com.house.agents.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wechat.ingest")
public class WechatIngestProperties {
    /**
     * Whether collector ingest endpoints are enabled.
     */
    private boolean enabled = false;

    /**
     * SHA-256 hex of the collector secret.
     */
    private String tokenHash = "";

    /**
     * Acceptable request timestamp skew in seconds.
     */
    private long allowedClockSkewSeconds = 300;

    /**
     * Replay cache ttl in seconds.
     */
    private long replayTtlSeconds = 600;
}
