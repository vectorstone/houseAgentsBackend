package com.house.agents.entity.vo;

import lombok.Data;

@Data
public class WechatDraftAttachmentStageVo {
    private String sourceKey;
    private String mediaHash;
    private String fileName;
    private String mimeType;
    private Integer contentType;
    private String collectorLocalPath;
    private String draftOssUrl;
    private String draftOssObjectKey;
    private String correlationStatus;
    private Double correlationScore;
    private String correlationReason;
    private String rawMetadataJson;
}
