package com.house.agents.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "WechatHouseDraft", description = "微信房源草稿")
public class WechatHouseDraft implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("source_key")
    private String sourceKey;

    @TableField("source_platform")
    private String sourcePlatform;

    @TableField("source_group_key")
    private String sourceGroupKey;

    @TableField("source_group_name")
    private String sourceGroupName;

    @TableField("sender_key")
    private String senderKey;

    @TableField("sender_display_name")
    private String senderDisplayName;

    @TableField("message_time")
    private String messageTime;

    @TableField("collector_receive_time")
    private String collectorReceiveTime;

    @TableField("message_order")
    private Integer messageOrder;

    @TableField("visible_text")
    private String visibleText;

    @TableField("raw_payload_json")
    private String rawPayloadJson;

    @TableField("extracted_json")
    private String extractedJson;

    @TableField("field_confidence_json")
    private String fieldConfidenceJson;

    @TableField("overall_confidence")
    private Double overallConfidence;

    @TableField("draft_status")
    private String draftStatus;

    @TableField("reviewer_user_id")
    private Long reviewerUserId;

    @TableField("review_note")
    private String reviewNote;

    @TableField("created_house_id")
    private Long createdHouseId;

    @TableField("failure_reason")
    private String failureReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;
}
