package com.house.agents.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(name = "WechatDraftUpdateVo", description = "微信房源草稿人工审核保存请求")
@Data
public class WechatDraftUpdateVo {

    @Schema(description = "结构化草稿 JSON")
    private String extractedJson;

    @Schema(description = "字段置信度 JSON")
    private String fieldConfidenceJson;

    @Schema(description = "审核备注")
    private String reviewNote;
}
