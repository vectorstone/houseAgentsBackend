package com.house.agents.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.house.agents.entity.WechatDraftAttachment;
import com.house.agents.entity.WechatHouseDraft;
import com.house.agents.entity.vo.WechatDraftAttachmentIngestVo;
import com.house.agents.entity.vo.WechatDraftAttachmentStageVo;
import com.house.agents.entity.vo.WechatDraftIngestVo;
import com.house.agents.service.WechatDraftAttachmentService;
import com.house.agents.service.WechatDraftExtractionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WechatHouseDraftServiceImplTest {

    @Test
    public void ingestDraftShouldUpsertBySourceKeyAndForwardAttachments() throws Exception {
        InMemoryWechatHouseDraftService draftService = new InMemoryWechatHouseDraftService();
        RecordingAttachmentService attachmentService = new RecordingAttachmentService();
        injectAttachmentService(draftService, attachmentService);
        injectExtractionService(draftService, request -> {
            WechatDraftExtractionService.ExtractionOutcome outcome = new WechatDraftExtractionService.ExtractionOutcome();
            outcome.setSchemaValid(true);
            outcome.setRecommendedDraftStatus("EXTRACTED");
            outcome.setExtracted(true);
            return outcome;
        });

        WechatDraftIngestVo first = buildDraft("source-1", "原始文本1", 0.81, "att-1");
        WechatHouseDraft created = draftService.ingestDraft(first);

        Assertions.assertNotNull(created.getId());
        Assertions.assertEquals("EXTRACTED", created.getDraftStatus());
        Assertions.assertEquals(1, draftService.drafts.size());
        Assertions.assertEquals(created.getId(), attachmentService.lastDraftId);
        Assertions.assertEquals(1, attachmentService.lastAttachments.size());
        Assertions.assertEquals("att-1", attachmentService.lastAttachments.get(0).getSourceKey());

        Long originalId = created.getId();
        WechatDraftIngestVo second = buildDraft("source-1", "原始文本2", 0.93, "att-2");
        WechatHouseDraft updated = draftService.ingestDraft(second);

        Assertions.assertEquals(originalId, updated.getId());
        Assertions.assertEquals(1, draftService.drafts.size());
        Assertions.assertEquals("原始文本2", updated.getVisibleText());
        Assertions.assertEquals(0.93, updated.getOverallConfidence());
        Assertions.assertEquals("att-2", attachmentService.lastAttachments.get(0).getSourceKey());
    }

    private static WechatDraftIngestVo buildDraft(String sourceKey, String visibleText, double confidence, String attachmentSourceKey) {
        WechatDraftAttachmentIngestVo attachment = new WechatDraftAttachmentIngestVo();
        attachment.setSourceKey(attachmentSourceKey);

        WechatDraftIngestVo draft = new WechatDraftIngestVo();
        draft.setSourceKey(sourceKey);
        draft.setSourcePlatform("WECHAT_PC");
        draft.setSourceGroupKey("group-1");
        draft.setSourceGroupName("租房群");
        draft.setSenderKey("sender-1");
        draft.setSenderDisplayName("张三");
        draft.setMessageTime("2026-06-02T00:00:00Z");
        draft.setCollectorReceiveTime("2026-06-02T00:00:01Z");
        draft.setMessageOrder(1);
        draft.setVisibleText(visibleText);
        draft.setRawPayloadJson("{}");
        draft.setExtractedJson("{}");
        draft.setFieldConfidenceJson("{}");
        draft.setOverallConfidence(confidence);
        draft.setAttachments(List.of(attachment));
        return draft;
    }

    private static void injectAttachmentService(WechatHouseDraftServiceImpl draftService, WechatDraftAttachmentService attachmentService) throws Exception {
        Field field = WechatHouseDraftServiceImpl.class.getDeclaredField("wechatDraftAttachmentService");
        field.setAccessible(true);
        field.set(draftService, attachmentService);
    }

    private static void injectExtractionService(WechatHouseDraftServiceImpl draftService, WechatDraftExtractionService extractionService) throws Exception {
        Field field = WechatHouseDraftServiceImpl.class.getDeclaredField("wechatDraftExtractionService");
        field.setAccessible(true);
        field.set(draftService, extractionService);
    }

    static class InMemoryWechatHouseDraftService extends WechatHouseDraftServiceImpl {
        private final Map<String, WechatHouseDraft> drafts = new LinkedHashMap<>();
        private long sequence = 1000L;

        @Override
        public WechatHouseDraft getOne(Wrapper<WechatHouseDraft> queryWrapper, boolean throwEx) {
            List<WechatHouseDraft> values = new ArrayList<>(drafts.values());
            return values.isEmpty() ? null : values.get(values.size() - 1);
        }

        @Override
        public boolean saveOrUpdate(WechatHouseDraft entity) {
            if (entity.getId() == null) {
                entity.setId(++sequence);
            }
            drafts.put(entity.getSourceKey(), entity);
            return true;
        }
    }

    static class RecordingAttachmentService extends InMemoryWechatDraftAttachmentService implements WechatDraftAttachmentService {
        private Long lastDraftId;
        private List<WechatDraftAttachmentIngestVo> lastAttachments = List.of();

        @Override
        public void saveOrUpdateByDraft(Long draftId, List<WechatDraftAttachmentIngestVo> attachments) {
            lastDraftId = draftId;
            lastAttachments = attachments;
        }
    }

    static class InMemoryWechatDraftAttachmentService extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.house.agents.mapper.WechatDraftAttachmentMapper, WechatDraftAttachment>
            implements WechatDraftAttachmentService {
        @Override
        public void saveOrUpdateByDraft(Long draftId, List<WechatDraftAttachmentIngestVo> attachments) {
        }

        @Override
        public WechatDraftAttachment stageAttachment(Long draftId, WechatDraftAttachmentStageVo request) {
            return null;
        }
    }
}
