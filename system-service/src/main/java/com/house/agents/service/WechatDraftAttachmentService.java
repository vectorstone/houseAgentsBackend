package com.house.agents.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.house.agents.entity.WechatDraftAttachment;
import com.house.agents.entity.vo.WechatDraftAttachmentIngestVo;
import com.house.agents.entity.vo.WechatDraftAttachmentStageVo;

import java.util.List;

public interface WechatDraftAttachmentService extends IService<WechatDraftAttachment> {
    void saveOrUpdateByDraft(Long draftId, List<WechatDraftAttachmentIngestVo> attachments);

    WechatDraftAttachment stageAttachment(Long draftId, WechatDraftAttachmentStageVo request);
}
