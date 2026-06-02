package com.house.agents.entity.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WechatDraftIngestVo {
    private String sourceKey;
    private String sourcePlatform;
    private String sourceGroupKey;
    private String sourceGroupName;
    private String senderKey;
    private String senderDisplayName;
    private String messageTime;
    private String collectorReceiveTime;
    private Integer messageOrder;
    private String visibleText;
    private String rawPayloadJson;
    private String extractedJson;
    private String fieldConfidenceJson;
    private Double overallConfidence;
    private List<WechatDraftAttachmentIngestVo> attachments = new ArrayList<>();
}
