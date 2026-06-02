package com.house.agents.service.impl;

import com.house.agents.HouseApplication;
import com.house.agents.entity.House;
import com.house.agents.entity.HouseAttachment;
import com.house.agents.entity.WechatDraftAttachment;
import com.house.agents.entity.WechatHouseDraft;
import com.house.agents.entity.vo.WechatDraftApproveVo;
import com.house.agents.entity.vo.WechatDraftAttachmentIngestVo;
import com.house.agents.entity.vo.WechatDraftAttachmentStageVo;
import com.house.agents.entity.vo.WechatDraftIngestVo;
import com.house.agents.mapper.HouseAttachmentMapper;
import com.house.agents.mapper.HouseMapper;
import com.house.agents.mapper.WechatDraftAttachmentMapper;
import com.house.agents.mapper.WechatHouseDraftMapper;
import com.house.agents.service.WechatHouseDraftService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = HouseApplication.class)
@TestPropertySource(properties = {
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.url=jdbc:h2:mem:wechatapprove;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "jasypt.encryptor.enabled=false",
        "spring.data.redis.repositories.enabled=false"
})
public class WechatHouseDraftApprovalIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private WechatHouseDraftService wechatHouseDraftService;
    @Autowired
    private WechatHouseDraftMapper draftMapper;
    @Autowired
    private WechatDraftAttachmentMapper draftAttachmentMapper;
    @Autowired
    private HouseMapper houseMapper;
    @Autowired
    private HouseAttachmentMapper houseAttachmentMapper;

    @BeforeEach
    public void setUp() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS house_attachment");
        jdbcTemplate.execute("DROP TABLE IF EXISTS house");
        jdbcTemplate.execute("DROP TABLE IF EXISTS wechat_draft_attachment");
        jdbcTemplate.execute("DROP TABLE IF EXISTS wechat_house_draft");
        jdbcTemplate.execute("CREATE TABLE wechat_house_draft (id BIGINT PRIMARY KEY, source_key VARCHAR(128) NOT NULL, source_platform VARCHAR(32), source_group_key VARCHAR(128), source_group_name VARCHAR(255), sender_key VARCHAR(128), sender_display_name VARCHAR(255), message_time VARCHAR(64), collector_receive_time VARCHAR(64), message_order INT, visible_text CLOB, raw_payload_json CLOB, extracted_json CLOB, field_confidence_json CLOB, overall_confidence DECIMAL(5,2), draft_status VARCHAR(32), reviewer_user_id BIGINT, review_note VARCHAR(1024), created_house_id BIGINT, failure_reason VARCHAR(1024), create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, is_deleted BOOLEAN DEFAULT FALSE, CONSTRAINT uk_wechat_house_draft_source_key UNIQUE (source_key))");
        jdbcTemplate.execute("CREATE TABLE wechat_draft_attachment (id BIGINT PRIMARY KEY, draft_id BIGINT NOT NULL, source_key VARCHAR(128) NOT NULL, media_hash VARCHAR(128), file_name VARCHAR(255), mime_type VARCHAR(128), content_type INT DEFAULT -1, collector_local_path VARCHAR(1024), draft_oss_url VARCHAR(1024), draft_oss_object_key VARCHAR(512), promotion_status VARCHAR(32) DEFAULT 'STAGED', promoted_house_attachment_id BIGINT, promoted_oss_url VARCHAR(1024), promoted_oss_object_key VARCHAR(512), correlation_status VARCHAR(32), correlation_score DECIMAL(5,2), correlation_reason VARCHAR(1024), raw_metadata_json CLOB, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, is_deleted BOOLEAN DEFAULT FALSE, CONSTRAINT uk_wechat_draft_attachment_source_key UNIQUE (draft_id, source_key))");
        jdbcTemplate.execute("CREATE TABLE house (id BIGINT PRIMARY KEY, user_id BIGINT, community VARCHAR(255), subway VARCHAR(255), room_number VARCHAR(255), rent DECIMAL(10,2), orientation VARCHAR(255), keyOrPassword VARCHAR(255), remark CLOB, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, is_deleted BOOLEAN DEFAULT FALSE, houseStatus INT, landlordName VARCHAR(255))");
        jdbcTemplate.execute("CREATE TABLE house_attachment (id BIGINT PRIMARY KEY, user_id BIGINT, house_id BIGINT, username VARCHAR(255), image_name VARCHAR(255), url VARCHAR(1024), description VARCHAR(255), create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, is_deleted BOOLEAN DEFAULT FALSE, contentType INT)");
    }

    @Test
    public void approveDraftShouldCreateHousePromoteAttachmentAndStayIdempotent() {
        WechatDraftIngestVo ingestVo = new WechatDraftIngestVo();
        ingestVo.setSourceKey("source-1");
        ingestVo.setSourcePlatform("WECHAT_PC");
        ingestVo.setSourceGroupKey("group-1");
        ingestVo.setSourceGroupName("租房群");
        ingestVo.setSenderKey("sender-1");
        ingestVo.setSenderDisplayName("张三");
        ingestVo.setMessageTime("2026-06-02T00:00:00Z");
        ingestVo.setCollectorReceiveTime("2026-06-02T00:00:01Z");
        ingestVo.setMessageOrder(7);
        ingestVo.setVisibleText("一室一厅 3500");
        ingestVo.setRawPayloadJson("{}");
        ingestVo.setOverallConfidence(0.88);
        WechatDraftAttachmentIngestVo attachment = new WechatDraftAttachmentIngestVo();
        attachment.setSourceKey("att-1");
        attachment.setMediaHash("hash-1");
        attachment.setFileName("room1.jpg");
        attachment.setMimeType("image/jpeg");
        attachment.setContentType(0);
        attachment.setDraftOssUrl("https://draft/room1.jpg");
        attachment.setDraftOssObjectKey("wechat-draft/1/room1.jpg");
        attachment.setCorrelationStatus("matched");
        ingestVo.setAttachments(List.of(attachment));

        WechatHouseDraft draft = wechatHouseDraftService.ingestDraft(ingestVo);
        Long draftId = draft.getId();

        WechatDraftApproveVo approveVo = new WechatDraftApproveVo();
        approveVo.setTargetUserId(9001L);
        approveVo.setCommunity("测试小区");
        approveVo.setSubway("6号线");
        approveVo.setRoomNumber("1室1厅");
        approveVo.setRent(new BigDecimal("3500"));
        approveVo.setOrientation("南");
        approveVo.setKeyOrPassword("1234");
        approveVo.setRemark("可押一付一");
        approveVo.setLandlordName("张三");
        approveVo.setReviewNote("通过");

        WechatHouseDraft approved = wechatHouseDraftService.approveDraft(draftId, approveVo, 7001L, "审核员");
        assertNotNull(approved.getCreatedHouseId());
        assertEquals("APPROVED", approved.getDraftStatus());
        assertEquals(1, houseMapper.selectList(null).size());
        House house = houseMapper.selectList(null).get(0);
        assertEquals("测试小区", house.getCommunity());
        assertEquals(9001L, house.getUserId());
        assertEquals(1, houseAttachmentMapper.selectList(null).size());
        HouseAttachment promotedAttachment = houseAttachmentMapper.selectList(null).get(0);
        assertEquals(house.getId(), promotedAttachment.getHouseId());
        assertEquals("https://draft/room1.jpg", promotedAttachment.getUrl());

        WechatDraftAttachment draftAttachment = draftAttachmentMapper.selectList(null).get(0);
        assertEquals("PROMOTED", draftAttachment.getPromotionStatus());
        assertEquals(promotedAttachment.getId(), draftAttachment.getPromotedHouseAttachmentId());

        WechatHouseDraft approvedAgain = wechatHouseDraftService.approveDraft(draftId, approveVo, 7001L, "审核员");
        assertEquals(approved.getCreatedHouseId(), approvedAgain.getCreatedHouseId());
        assertEquals(1, houseMapper.selectList(null).size());
        assertEquals(1, houseAttachmentMapper.selectList(null).size());
    }

    @Test
    public void stageAttachmentShouldUpsertDraftAttachmentAndMoveDraftToNeedsReview() {
        WechatDraftIngestVo ingestVo = new WechatDraftIngestVo();
        ingestVo.setSourceKey("source-2");
        ingestVo.setSourcePlatform("WECHAT_PC");
        ingestVo.setVisibleText("看图联系");
        ingestVo.setRawPayloadJson("{}");
        WechatHouseDraft draft = wechatHouseDraftService.ingestDraft(ingestVo);

        WechatDraftAttachmentStageVo stageVo = new WechatDraftAttachmentStageVo();
        stageVo.setSourceKey("att-2");
        stageVo.setFileName("room2.mp4");
        stageVo.setContentType(1);
        stageVo.setCollectorLocalPath("C:/evidence/room2.mp4");
        stageVo.setDraftOssUrl("https://draft/room2.mp4");
        stageVo.setDraftOssObjectKey("wechat-draft/2/room2.mp4");
        stageVo.setCorrelationStatus("matched");
        wechatHouseDraftService.stageAttachment(draft.getId(), stageVo);

        WechatHouseDraft updatedDraft = draftMapper.selectById(draft.getId());
        assertEquals("NEEDS_REVIEW", updatedDraft.getDraftStatus());
        assertEquals(1, draftAttachmentMapper.selectList(null).size());
        WechatDraftAttachment staged = draftAttachmentMapper.selectList(null).get(0);
        assertEquals("STAGED", staged.getPromotionStatus());
        assertEquals("https://draft/room2.mp4", staged.getDraftOssUrl());
    }

    @Test
    public void approveDraftShouldKeepHouseAndAllowRetryWhenAttachmentPromotionPartiallyFails() {
        WechatDraftIngestVo ingestVo = new WechatDraftIngestVo();
        ingestVo.setSourceKey("source-3");
        ingestVo.setSourcePlatform("WECHAT_PC");
        ingestVo.setVisibleText("两张图");
        ingestVo.setRawPayloadJson("{}");
        ingestVo.setAttachments(List.of(
                buildAttachment("att-ok", "ok.jpg", "https://draft/ok.jpg", "wechat-draft/3/ok.jpg", 0),
                buildAttachment("att-fail", "fail.jpg", null, null, 0)
        ));
        WechatHouseDraft draft = wechatHouseDraftService.ingestDraft(ingestVo);

        WechatDraftApproveVo approveVo = new WechatDraftApproveVo();
        approveVo.setTargetUserId(9002L);
        approveVo.setCommunity("重试小区");
        approveVo.setRoomNumber("2室1厅");
        approveVo.setReviewNote("首次审批");

        WechatHouseDraft firstApprove = wechatHouseDraftService.approveDraft(draft.getId(), approveVo, 7002L, "审核员A");
        assertNotNull(firstApprove.getCreatedHouseId());
        assertEquals("NEEDS_REVIEW", firstApprove.getDraftStatus());
        assertEquals(1, houseMapper.selectList(null).size());
        assertEquals(1, houseAttachmentMapper.selectList(null).size());

        List<WechatDraftAttachment> afterFirst = draftAttachmentMapper.selectList(null);
        WechatDraftAttachment ok = afterFirst.stream().filter(a -> "att-ok".equals(a.getSourceKey())).findFirst().orElseThrow();
        WechatDraftAttachment failed = afterFirst.stream().filter(a -> "att-fail".equals(a.getSourceKey())).findFirst().orElseThrow();
        assertEquals("PROMOTED", ok.getPromotionStatus());
        assertEquals("FAILED", failed.getPromotionStatus());

        WechatDraftAttachmentStageVo retryStage = new WechatDraftAttachmentStageVo();
        retryStage.setSourceKey("att-fail");
        retryStage.setFileName("fail.jpg");
        retryStage.setContentType(0);
        retryStage.setDraftOssUrl("https://draft/fail.jpg");
        retryStage.setDraftOssObjectKey("wechat-draft/3/fail.jpg");
        retryStage.setCorrelationStatus("matched");
        wechatHouseDraftService.stageAttachment(draft.getId(), retryStage);

        WechatHouseDraft secondApprove = wechatHouseDraftService.approveDraft(draft.getId(), approveVo, 7002L, "审核员A");
        assertEquals(firstApprove.getCreatedHouseId(), secondApprove.getCreatedHouseId());
        assertEquals("APPROVED", secondApprove.getDraftStatus());
        assertEquals(1, houseMapper.selectList(null).size());
        assertEquals(2, houseAttachmentMapper.selectList(null).size());

        List<WechatDraftAttachment> afterRetry = draftAttachmentMapper.selectList(null);
        WechatDraftAttachment retried = afterRetry.stream().filter(a -> "att-fail".equals(a.getSourceKey())).findFirst().orElseThrow();
        assertEquals("PROMOTED", retried.getPromotionStatus());
        assertNotNull(retried.getPromotedHouseAttachmentId());
    }

    private WechatDraftAttachmentIngestVo buildAttachment(String sourceKey, String fileName, String draftOssUrl, String draftOssObjectKey, int contentType) {
        WechatDraftAttachmentIngestVo attachment = new WechatDraftAttachmentIngestVo();
        attachment.setSourceKey(sourceKey);
        attachment.setMediaHash("hash-" + sourceKey);
        attachment.setFileName(fileName);
        attachment.setMimeType("image/jpeg");
        attachment.setContentType(contentType);
        attachment.setDraftOssUrl(draftOssUrl);
        attachment.setDraftOssObjectKey(draftOssObjectKey);
        attachment.setCorrelationStatus("matched");
        return attachment;
    }
}
