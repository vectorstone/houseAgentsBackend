package com.house.agents.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.house.agents.entity.WechatDraftAttachment;
import com.house.agents.entity.vo.WechatDraftAttachmentIngestVo;
import com.house.agents.entity.vo.WechatDraftAttachmentStageVo;
import com.house.agents.mapper.WechatDraftAttachmentMapper;
import com.house.agents.service.WechatDraftAttachmentService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class WechatDraftAttachmentServiceImpl extends ServiceImpl<WechatDraftAttachmentMapper, WechatDraftAttachment>
        implements WechatDraftAttachmentService {

    @Override
    public void saveOrUpdateByDraft(Long draftId, List<WechatDraftAttachmentIngestVo> attachments) {
        for (WechatDraftAttachmentIngestVo item : attachments == null ? Collections.<WechatDraftAttachmentIngestVo>emptyList() : attachments) {
            WechatDraftAttachment attachment = this.getOne(
                    new LambdaQueryWrapper<WechatDraftAttachment>()
                            .eq(WechatDraftAttachment::getDraftId, draftId)
                            .eq(WechatDraftAttachment::getSourceKey, item.getSourceKey()),
                    false
            );
            if (attachment == null) {
                attachment = new WechatDraftAttachment();
                attachment.setDraftId(draftId);
                attachment.setSourceKey(item.getSourceKey());
                attachment.setPromotionStatus("STAGED");
            }
            attachment.setMediaHash(item.getMediaHash());
            attachment.setFileName(item.getFileName());
            attachment.setMimeType(item.getMimeType());
            attachment.setContentType(item.getContentType());
            attachment.setCollectorLocalPath(item.getCollectorLocalPath());
            attachment.setDraftOssUrl(item.getDraftOssUrl());
            attachment.setDraftOssObjectKey(item.getDraftOssObjectKey());
            attachment.setCorrelationStatus(item.getCorrelationStatus());
            attachment.setCorrelationScore(item.getCorrelationScore());
            attachment.setCorrelationReason(item.getCorrelationReason());
            attachment.setRawMetadataJson(item.getRawMetadataJson());
            this.saveOrUpdate(attachment);
        }
    }
    @Override
    public WechatDraftAttachment stageAttachment(Long draftId, WechatDraftAttachmentStageVo item) {
        WechatDraftAttachment attachment = this.getOne(
                new LambdaQueryWrapper<WechatDraftAttachment>()
                        .eq(WechatDraftAttachment::getDraftId, draftId)
                        .eq(WechatDraftAttachment::getSourceKey, item.getSourceKey()),
                false
        );
        if (attachment == null) {
            attachment = new WechatDraftAttachment();
            attachment.setDraftId(draftId);
            attachment.setSourceKey(item.getSourceKey());
            attachment.setPromotionStatus("STAGED");
        }
        attachment.setMediaHash(item.getMediaHash());
        attachment.setFileName(item.getFileName());
        attachment.setMimeType(item.getMimeType());
        attachment.setContentType(item.getContentType());
        attachment.setCollectorLocalPath(item.getCollectorLocalPath());
        attachment.setDraftOssUrl(item.getDraftOssUrl());
        attachment.setDraftOssObjectKey(item.getDraftOssObjectKey());
        attachment.setCorrelationStatus(item.getCorrelationStatus());
        attachment.setCorrelationScore(item.getCorrelationScore());
        attachment.setCorrelationReason(item.getCorrelationReason());
        attachment.setRawMetadataJson(item.getRawMetadataJson());
        this.saveOrUpdate(attachment);
        return attachment;
    }

}
