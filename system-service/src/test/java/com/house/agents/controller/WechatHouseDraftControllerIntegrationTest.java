package com.house.agents.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.house.agents.HouseApplication;
import com.house.agents.entity.vo.WechatDraftIngestVo;
import com.house.agents.entity.WechatDraftAttachment;
import com.house.agents.entity.WechatHouseDraft;
import com.house.agents.mapper.WechatDraftAttachmentMapper;
import com.house.agents.mapper.WechatHouseDraftMapper;
import com.house.agents.utils.WechatIngestAuthUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = HouseApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "wechat.ingest.enabled=true",
        "wechat.ingest.tokenHash=collector-secret",
        "wechat.ingest.allowedClockSkewSeconds=300",
        "wechat.ingest.replayTtlSeconds=60",
        "spring.main.allow-bean-definition-overriding=true",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.url=jdbc:h2:mem:wechatdraft;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.redis.host=localhost",
        "spring.redis.port=6370",
        "jasypt.encryptor.enabled=false",
        "spring.data.redis.repositories.enabled=false"
})
public class WechatHouseDraftControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private WechatHouseDraftMapper draftMapper;

    @Autowired
    private WechatDraftAttachmentMapper attachmentMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean(name = "redisTemplate")
    private RedisTemplate redisTemplate;

    private final Map<String, Object> replayStore = new java.util.concurrent.ConcurrentHashMap<>();

    @BeforeEach
    public void setUp() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS wechat_draft_attachment");
        jdbcTemplate.execute("DROP TABLE IF EXISTS wechat_house_draft");
        jdbcTemplate.execute("CREATE TABLE wechat_house_draft (" +
                "id BIGINT PRIMARY KEY," +
                "source_key VARCHAR(128) NOT NULL," +
                "source_platform VARCHAR(32)," +
                "source_group_key VARCHAR(128)," +
                "source_group_name VARCHAR(255)," +
                "sender_key VARCHAR(128)," +
                "sender_display_name VARCHAR(255)," +
                "message_time VARCHAR(64)," +
                "collector_receive_time VARCHAR(64)," +
                "message_order INT," +
                "visible_text CLOB," +
                "raw_payload_json CLOB," +
                "extracted_json CLOB," +
                "field_confidence_json CLOB," +
                "overall_confidence DECIMAL(5,2)," +
                "draft_status VARCHAR(32)," +
                "reviewer_user_id BIGINT," +
                "review_note VARCHAR(1024)," +
                "created_house_id BIGINT," +
                "failure_reason VARCHAR(1024)," +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "is_deleted BOOLEAN DEFAULT FALSE," +
                "CONSTRAINT uk_wechat_house_draft_source_key UNIQUE (source_key)" +
                ")");
        jdbcTemplate.execute("CREATE TABLE wechat_draft_attachment (" +
                "id BIGINT PRIMARY KEY," +
                "draft_id BIGINT NOT NULL," +
                "source_key VARCHAR(128) NOT NULL," +
                "media_hash VARCHAR(128)," +
                "file_name VARCHAR(255)," +
                "mime_type VARCHAR(128)," +
                "content_type INT DEFAULT -1," +
                "collector_local_path VARCHAR(1024)," +
                "draft_oss_url VARCHAR(1024)," +
                "draft_oss_object_key VARCHAR(512)," +
                "promotion_status VARCHAR(32) DEFAULT 'STAGED'," +
                "promoted_house_attachment_id BIGINT," +
                "promoted_oss_url VARCHAR(1024)," +
                "promoted_oss_object_key VARCHAR(512)," +
                "correlation_status VARCHAR(32)," +
                "correlation_score DECIMAL(5,2)," +
                "correlation_reason VARCHAR(1024)," +
                "raw_metadata_json CLOB," +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "is_deleted BOOLEAN DEFAULT FALSE," +
                "CONSTRAINT uk_wechat_draft_attachment_source_key UNIQUE (draft_id, source_key)" +
                ")");

        replayStore.clear();
        ValueOperations<String, Object> valueOperations = Mockito.mock(ValueOperations.class);
        BoundValueOperations<String, Object> boundValueOperations = Mockito.mock(BoundValueOperations.class);
        Mockito.when(redisTemplate.hasKey(Mockito.anyString()))
                .thenAnswer(invocation -> replayStore.containsKey(invocation.getArgument(0)));
        Mockito.doAnswer(invocation -> {
            replayStore.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(valueOperations).set(Mockito.anyString(), Mockito.any(), Mockito.anyLong(), Mockito.any(TimeUnit.class));
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(redisTemplate.boundValueOps(Mockito.anyString())).thenReturn(boundValueOperations);
        Mockito.when(boundValueOperations.get()).thenAnswer(invocation -> buildAdminUser());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin",
                null,
                List.of(
                        new SimpleGrantedAuthority("bnt.wechatDraft.list"),
                        new SimpleGrantedAuthority("bnt.wechatDraft.view"),
                        new SimpleGrantedAuthority("bnt.wechatDraft.update"),
                        new SimpleGrantedAuthority("bnt.wechatDraft.approve"),
                        new SimpleGrantedAuthority("bnt.wechatDraft.reject")
                )
        ));
    }

    @Test
    public void ingestShouldPersistDraftAndAttachmentAndRemainIdempotentBySourceKey() throws Exception {
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("sourceKey", "source-1");
        payload.put("sourcePlatform", "WECHAT_PC");
        payload.put("sourceGroupKey", "group-1");
        payload.put("sourceGroupName", "租房群");
        payload.put("senderKey", "sender-1");
        payload.put("senderDisplayName", "张三");
        payload.put("messageTime", "2026-06-02T00:00:00Z");
        payload.put("collectorReceiveTime", "2026-06-02T00:00:01Z");
        payload.put("messageOrder", 7);
        payload.put("visibleText", "一室一厅 3500");
        payload.put("rawPayloadJson", "{\"hint\":\"image\"}");
        payload.put("extractedJson", "");
        payload.put("fieldConfidenceJson", "");
        payload.put("overallConfidence", 0.91);
        payload.put("attachments", List.of(new java.util.LinkedHashMap<String, Object>(Map.ofEntries(
                Map.entry("sourceKey", "cand-1"),
                Map.entry("mediaHash", "hash-1"),
                Map.entry("fileName", "room1.jpg"),
                Map.entry("mimeType", "image/jpeg"),
                Map.entry("contentType", 0),
                Map.entry("collectorLocalPath", "C:/evidence/room1.jpg"),
                Map.entry("draftOssUrl", ""),
                Map.entry("draftOssObjectKey", ""),
                Map.entry("correlationStatus", "matched"),
                Map.entry("correlationScore", 0.91),
                Map.entry("correlationReason", "single candidate"),
                Map.entry("rawMetadataJson", "{\"parent_dir\":\"Image\"}")
        ))));
        String body = canonicalBody(payload);

        String timestamp1 = String.valueOf(Instant.now().getEpochSecond());
        String idempotencyKey1 = "idem-1";
        String signature1 = sign(body, timestamp1, idempotencyKey1);

        mockMvc.perform(post("/api/wechat-house-drafts/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Collector-Timestamp", timestamp1)
                        .header("X-Idempotency-Key", idempotencyKey1)
                        .header("X-Collector-Signature", signature1)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.sourceKey").value("source-1"))
                .andExpect(jsonPath("$.data.draftStatus").value("EXTRACTED"));

        List<WechatHouseDraft> draftsAfterFirst = draftMapper.selectList(null);
        assertEquals(1, draftsAfterFirst.size());
        WechatHouseDraft createdDraft = draftsAfterFirst.get(0);
        assertEquals("source-1", createdDraft.getSourceKey());
        assertEquals("EXTRACTED", createdDraft.getDraftStatus());
        assertEquals("一室一厅 3500", createdDraft.getVisibleText());
        assertEquals(0.93, createdDraft.getOverallConfidence());
        assertEquals(1, attachmentMapper.selectList(null).size());
        WechatDraftAttachment attachment = attachmentMapper.selectList(null).get(0);
        assertEquals(createdDraft.getId(), attachment.getDraftId());
        assertEquals("cand-1", attachment.getSourceKey());
        assertEquals("STAGED", attachment.getPromotionStatus());
        assertEquals("matched", attachment.getCorrelationStatus());

        payload.put("visibleText", "一室一厅 3600");
        java.util.Map<String, Object> attachmentPayload = (java.util.Map<String, Object>) ((java.util.List<?>) payload.get("attachments")).get(0);
        attachmentPayload.put("mediaHash", "hash-2");
        String body2 = canonicalBody(payload);
        String timestamp2 = String.valueOf(Instant.now().getEpochSecond());
        String idempotencyKey2 = "idem-2";
        String signature2 = sign(body2, timestamp2, idempotencyKey2);

        mockMvc.perform(post("/api/wechat-house-drafts/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Collector-Timestamp", timestamp2)
                        .header("X-Idempotency-Key", idempotencyKey2)
                        .header("X-Collector-Signature", signature2)
                        .content(body2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.sourceKey").value("source-1"));

        List<WechatHouseDraft> draftsAfterSecond = draftMapper.selectList(null);
        assertEquals(1, draftsAfterSecond.size());
        WechatHouseDraft updatedDraft = draftsAfterSecond.get(0);
        assertEquals(createdDraft.getId(), updatedDraft.getId());
        assertEquals("一室一厅 3600", updatedDraft.getVisibleText());
        assertEquals(0.93, updatedDraft.getOverallConfidence());

        List<WechatDraftAttachment> attachmentsAfterSecond = attachmentMapper.selectList(null);
        assertEquals(1, attachmentsAfterSecond.size());
        WechatDraftAttachment updatedAttachment = attachmentsAfterSecond.get(0);
        assertEquals("hash-2", updatedAttachment.getMediaHash());
        assertEquals("matched", updatedAttachment.getCorrelationStatus());

        assertTrue(replayStore.containsKey("wechat:ingest:replay:idem-1"));
        assertTrue(replayStore.containsKey("wechat:ingest:replay:idem-2"));
        assertNotNull(updatedDraft.getId());
    }


    @Test
    public void listDetailAndUpdateShouldUseRealReviewLoopApis() throws Exception {
        jdbcTemplate.update("INSERT INTO wechat_house_draft (id, source_key, source_platform, source_group_name, sender_display_name, visible_text, draft_status, raw_payload_json, create_time, update_time, is_deleted) VALUES (?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,FALSE)",
                1001L, "source-review-1", "WECHAT_PC", "租房群", "李四", "朝南一居 4200", "PENDING", "{}");
        jdbcTemplate.update("INSERT INTO wechat_draft_attachment (id, draft_id, source_key, file_name, mime_type, content_type, draft_oss_url, promotion_status, correlation_status, correlation_score, create_time, update_time, is_deleted) VALUES (?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,FALSE)",
                2001L, 1001L, "att-review-1", "room.jpg", "image/jpeg", 0, "https://draft/review-room.jpg", "STAGED", "matched", 0.92);

        mockMvc.perform(get("/api/wechat-house-drafts/page/1/10")
                        .header("token", "admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.records[0].id").value(1001L))
                .andExpect(jsonPath("$.data.items.records[0].correlationStatus").value("matched"));

        mockMvc.perform(get("/api/wechat-house-drafts/1001")
                        .header("token", "admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1001L))
                .andExpect(jsonPath("$.data.form").isMap())
                .andExpect(jsonPath("$.data.evidences[1].promotionStatus").value("STAGED"));

        String updateBody = "{\"extractedJson\":\"{\\\"community\\\":\\\"未来城\\\",\\\"roomNumber\\\":\\\"1室1厅\\\"}\",\"fieldConfidenceJson\":\"{\\\"community\\\":0.88}\",\"reviewNote\":\"人工补充\"}";
        mockMvc.perform(put("/api/wechat-house-drafts/1001")
                        .header("token", "admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.draftStatus").value("NEEDS_REVIEW"));

        WechatHouseDraft updated = draftMapper.selectById(1001L);
        assertEquals("NEEDS_REVIEW", updated.getDraftStatus());
        assertTrue(updated.getExtractedJson().contains("未来城"));
        assertEquals("人工补充", updated.getReviewNote());
    }

    private com.house.agents.entity.SysUser buildAdminUser() {
        com.house.agents.entity.SysUser user = new com.house.agents.entity.SysUser();
        user.setId(7001L);
        user.setUsername("admin");
        user.setName("管理员");
        user.setUserPermsList(List.of("bnt.wechatDraft.list", "bnt.wechatDraft.view", "bnt.wechatDraft.update", "bnt.wechatDraft.approve", "bnt.wechatDraft.reject"));
        return user;
    }

    private String canonicalBody(Map<String, Object> payload) throws Exception {
        WechatDraftIngestVo vo = objectMapper.convertValue(payload, WechatDraftIngestVo.class);
        return objectMapper.writeValueAsString(vo);
    }

    private String sign(String body, String timestamp, String idempotencyKey) {
        String bodyHash = WechatIngestAuthUtils.sha256Hex(body.getBytes(StandardCharsets.UTF_8));
        return WechatIngestAuthUtils.hmacSha256Hex(
                "collector-secret",
                "POST" + "/api/wechat-house-drafts/ingest" + timestamp + idempotencyKey + bodyHash
        );
    }
}
