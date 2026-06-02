package com.house.agents.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.house.agents.entity.WechatHouseDraft;
import com.house.agents.entity.vo.WechatDraftApproveVo;
import com.house.agents.entity.vo.WechatDraftAttachmentStageVo;
import com.house.agents.entity.vo.WechatDraftIngestVo;
import com.house.agents.entity.vo.WechatDraftRejectVo;
import com.house.agents.entity.vo.WechatDraftUpdateVo;

public interface WechatHouseDraftService extends IService<WechatHouseDraft> {
    WechatHouseDraft ingestDraft(WechatDraftIngestVo request);

    void stageAttachment(Long draftId, WechatDraftAttachmentStageVo request);

    WechatHouseDraft approveDraft(Long draftId, WechatDraftApproveVo request, Long reviewerUserId, String reviewerName);

    WechatHouseDraft rejectDraft(Long draftId, WechatDraftRejectVo request, Long reviewerUserId);

    WechatHouseDraft updateDraft(Long draftId, WechatDraftUpdateVo request, Long reviewerUserId);
}
