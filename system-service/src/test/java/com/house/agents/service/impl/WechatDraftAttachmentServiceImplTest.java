package com.house.agents.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.house.agents.entity.WechatDraftAttachment;
import com.house.agents.entity.vo.WechatDraftAttachmentIngestVo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WechatDraftAttachmentServiceImplTest {

    @Test
    public void saveOrUpdateByDraftShouldUpsertByDraftIdAndSourceKey() {
        InMemoryWechatDraftAttachmentService service = new InMemoryWechatDraftAttachmentService();

        WechatDraftAttachmentIngestVo first = new WechatDraftAttachmentIngestVo();
        first.setSourceKey("cand-1");
        first.setMediaHash("hash-1");
        first.setFileName("room1.jpg");
        first.setContentType(0);
        first.setCollectorLocalPath("C:/evidence/room1.jpg");
        first.setCorrelationStatus("matched");
        first.setCorrelationScore(0.91);
        first.setCorrelationReason("first-pass");
        first.setRawMetadataJson("{\"a\":1}");

        service.saveOrUpdateByDraft(100L, List.of(first));
        Assertions.assertEquals(1, service.attachments.size());

        WechatDraftAttachment stored = service.attachments.get("100:cand-1");
        Assertions.assertNotNull(stored);
        Assertions.assertEquals("STAGED", stored.getPromotionStatus());
        Assertions.assertEquals("hash-1", stored.getMediaHash());
        Assertions.assertEquals("room1.jpg", stored.getFileName());

        WechatDraftAttachmentIngestVo second = new WechatDraftAttachmentIngestVo();
        second.setSourceKey("cand-1");
        second.setMediaHash("hash-2");
        second.setFileName("room1-updated.jpg");
        second.setContentType(1);
        second.setCollectorLocalPath("C:/evidence/room1-updated.jpg");
        second.setCorrelationStatus("matched");
        second.setCorrelationScore(0.97);
        second.setCorrelationReason("rescanned");
        second.setRawMetadataJson("{\"a\":2}");

        service.saveOrUpdateByDraft(100L, List.of(second));

        Assertions.assertEquals(1, service.attachments.size());
        WechatDraftAttachment updated = service.attachments.get("100:cand-1");
        Assertions.assertEquals("hash-2", updated.getMediaHash());
        Assertions.assertEquals("room1-updated.jpg", updated.getFileName());
        Assertions.assertEquals(1, updated.getContentType());
        Assertions.assertEquals("rescanned", updated.getCorrelationReason());
    }

    static class InMemoryWechatDraftAttachmentService extends WechatDraftAttachmentServiceImpl {
        private final Map<String, WechatDraftAttachment> attachments = new LinkedHashMap<>();

        @Override
        public WechatDraftAttachment getOne(Wrapper<WechatDraftAttachment> queryWrapper, boolean throwEx) {
            List<WechatDraftAttachment> values = new ArrayList<>(attachments.values());
            return values.isEmpty() ? null : values.get(values.size() - 1);
        }

        @Override
        public boolean saveOrUpdate(WechatDraftAttachment entity) {
            attachments.put(entity.getDraftId() + ":" + entity.getSourceKey(), entity);
            return true;
        }
    }
}
