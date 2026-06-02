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
@Schema(name = "WechatDraftAttachment", description = "微信房源草稿附件")
public class WechatDraftAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("draft_id")
    private Long draftId;

    @TableField("source_key")
    private String sourceKey;

    @TableField("media_hash")
    private String mediaHash;

    @TableField("file_name")
    private String fileName;

    @TableField("mime_type")
    private String mimeType;

    @TableField("content_type")
    private Integer contentType;

    @TableField("collector_local_path")
    private String collectorLocalPath;

    @TableField("draft_oss_url")
    private String draftOssUrl;

    @TableField("draft_oss_object_key")
    private String draftOssObjectKey;

    @TableField("promotion_status")
    private String promotionStatus;

    @TableField("promoted_house_attachment_id")
    private Long promotedHouseAttachmentId;

    @TableField("promoted_oss_url")
    private String promotedOssUrl;

    @TableField("promoted_oss_object_key")
    private String promotedOssObjectKey;

    @TableField("correlation_status")
    private String correlationStatus;

    @TableField("correlation_score")
    private Double correlationScore;

    @TableField("correlation_reason")
    private String correlationReason;

    @TableField("raw_metadata_json")
    private String rawMetadataJson;

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
