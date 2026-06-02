package com.house.agents.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.house.agents.Enum.HouseStatusEnum;
import com.house.agents.entity.House;
import com.house.agents.entity.HouseAttachment;
import com.house.agents.entity.WechatDraftAttachment;
import com.house.agents.entity.WechatHouseDraft;
import com.house.agents.entity.vo.WechatDraftApproveVo;
import com.house.agents.entity.vo.WechatDraftAttachmentStageVo;
import com.house.agents.entity.vo.WechatDraftIngestVo;
import com.house.agents.entity.vo.WechatDraftRejectVo;
import com.house.agents.entity.vo.WechatDraftUpdateVo;
import com.house.agents.mapper.WechatHouseDraftMapper;
import com.house.agents.service.HouseAttachmentService;
import com.house.agents.service.HouseService;
import com.house.agents.service.WechatDraftAttachmentService;
import com.house.agents.service.WechatDraftExtractionService;
import com.house.agents.service.WechatHouseDraftService;
import com.house.agents.utils.BusinessException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WechatHouseDraftServiceImpl extends ServiceImpl<WechatHouseDraftMapper, WechatHouseDraft>
        implements WechatHouseDraftService {

    @Autowired
    private WechatDraftAttachmentService wechatDraftAttachmentService;

    @Autowired
    private HouseService houseService;

    @Autowired
    private HouseAttachmentService houseAttachmentService;

    @Autowired
    private WechatDraftExtractionService wechatDraftExtractionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WechatHouseDraft ingestDraft(WechatDraftIngestVo request) {
        WechatDraftExtractionService.ExtractionOutcome extractionOutcome = wechatDraftExtractionService.enrich(request);
        WechatHouseDraft draft = this.getOne(
                new LambdaQueryWrapper<WechatHouseDraft>().eq(WechatHouseDraft::getSourceKey, request.getSourceKey()),
                false
        );
        if (draft == null) {
            draft = new WechatHouseDraft();
            draft.setSourceKey(request.getSourceKey());
        }
        if (StringUtils.isBlank(draft.getDraftStatus()) || StringUtils.equalsAny(draft.getDraftStatus(), "PENDING", "EXTRACTED")) {
            draft.setDraftStatus(resolveDraftStatus(extractionOutcome));
        }
        draft.setSourcePlatform(StringUtils.defaultIfBlank(request.getSourcePlatform(), "WECHAT_PC"));
        draft.setSourceGroupKey(request.getSourceGroupKey());
        draft.setSourceGroupName(request.getSourceGroupName());
        draft.setSenderKey(request.getSenderKey());
        draft.setSenderDisplayName(request.getSenderDisplayName());
        draft.setMessageTime(request.getMessageTime());
        draft.setCollectorReceiveTime(request.getCollectorReceiveTime());
        draft.setMessageOrder(request.getMessageOrder());
        draft.setVisibleText(request.getVisibleText());
        draft.setRawPayloadJson(request.getRawPayloadJson());
        draft.setExtractedJson(request.getExtractedJson());
        draft.setFieldConfidenceJson(request.getFieldConfidenceJson());
        draft.setOverallConfidence(request.getOverallConfidence());
        if (extractionOutcome != null && !extractionOutcome.isSchemaValid()) {
            draft.setFailureReason(extractionOutcome.getFailureReason());
        } else if (StringUtils.equalsAny(draft.getDraftStatus(), "PENDING", "EXTRACTED")) {
            draft.setFailureReason(null);
        }
        this.saveOrUpdate(draft);
        wechatDraftAttachmentService.saveOrUpdateByDraft(draft.getId(), request.getAttachments());
        return draft;
    }
    private String resolveDraftStatus(WechatDraftExtractionService.ExtractionOutcome extractionOutcome) {
        if (extractionOutcome == null) {
            return "PENDING";
        }
        if (!extractionOutcome.isSchemaValid()) {
            return "NEEDS_REVIEW";
        }
        return StringUtils.defaultIfBlank(extractionOutcome.getRecommendedDraftStatus(), "PENDING");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stageAttachment(Long draftId, WechatDraftAttachmentStageVo request) {
        WechatHouseDraft draft = requireDraft(draftId);
        wechatDraftAttachmentService.stageAttachment(draft.getId(), request);
        if (StringUtils.equals(draft.getDraftStatus(), "PENDING")) {
            draft.setDraftStatus("NEEDS_REVIEW");
            this.updateById(draft);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WechatHouseDraft approveDraft(Long draftId, WechatDraftApproveVo request, Long reviewerUserId, String reviewerName) {
        WechatHouseDraft draft = requireDraft(draftId);
        if (StringUtils.equalsAny(draft.getDraftStatus(), "REJECTED", "FAILED")) {
            throw new BusinessException("draft status does not allow approval");
        }

        House house;
        if (draft.getCreatedHouseId() != null) {
            house = houseService.getById(draft.getCreatedHouseId());
            if (house == null) {
                throw new BusinessException("created house not found for draft");
            }
        } else {
            house = new House();
            house.setUserId(request.getTargetUserId() == null ? reviewerUserId : request.getTargetUserId());
            house.setCommunity(request.getCommunity());
            house.setSubway(request.getSubway());
            house.setRoomNumber(request.getRoomNumber());
            house.setRent(request.getRent());
            house.setOrientation(request.getOrientation());
            house.setKeyOrPassword(request.getKeyOrPassword());
            house.setRemark(request.getRemark());
            house.setLandlordName(request.getLandlordName());
            house.setHouseStatus(request.getHouseStatus() == null ? HouseStatusEnum.HOUSE_UP.getCode() : request.getHouseStatus());
            houseService.save(house);
            draft.setCreatedHouseId(house.getId());
        }

        boolean hasPromotionFailure = promoteDraftAttachments(draftId, house, request, reviewerName);

        draft.setDraftStatus(hasPromotionFailure ? "NEEDS_REVIEW" : "APPROVED");
        draft.setReviewerUserId(reviewerUserId);
        draft.setReviewNote(request.getReviewNote());
        if (!hasPromotionFailure) {
            draft.setFailureReason(null);
        } else if (StringUtils.isBlank(draft.getFailureReason())) {
            draft.setFailureReason("attachment promotion failed");
        }
        this.updateById(draft);
        return draft;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WechatHouseDraft updateDraft(Long draftId, WechatDraftUpdateVo request, Long reviewerUserId) {
        WechatHouseDraft draft = requireDraft(draftId);
        draft.setExtractedJson(request.getExtractedJson());
        draft.setFieldConfidenceJson(request.getFieldConfidenceJson());
        draft.setReviewNote(request.getReviewNote());
        if (reviewerUserId != null) {
            draft.setReviewerUserId(reviewerUserId);
        }
        if (StringUtils.equalsAny(draft.getDraftStatus(), "PENDING", "EXTRACTED")) {
            draft.setDraftStatus("NEEDS_REVIEW");
        }
        this.updateById(draft);
        return draft;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WechatHouseDraft rejectDraft(Long draftId, WechatDraftRejectVo request, Long reviewerUserId) {
        WechatHouseDraft draft = requireDraft(draftId);
        draft.setDraftStatus("REJECTED");
        draft.setReviewerUserId(reviewerUserId);
        draft.setReviewNote(request.getReviewNote());
        draft.setFailureReason(request.getFailureReason());
        this.updateById(draft);
        return draft;
    }


    private boolean promoteDraftAttachments(Long draftId, House house, WechatDraftApproveVo request, String reviewerName) {
        List<WechatDraftAttachment> attachments = wechatDraftAttachmentService.list(
                Wrappers.lambdaQuery(WechatDraftAttachment.class).eq(WechatDraftAttachment::getDraftId, draftId)
        );
        if (CollectionUtils.isEmpty(attachments)) {
            return false;
        }

        boolean hasPromotionFailure = false;
        for (WechatDraftAttachment item : attachments) {
            if (StringUtils.equals(item.getPromotionStatus(), "PROMOTED") && item.getPromotedHouseAttachmentId() != null) {
                continue;
            }
            try {
                item.setPromotionStatus("PROMOTING");
                wechatDraftAttachmentService.updateById(item);

                String promotedUrl = StringUtils.firstNonBlank(item.getDraftOssUrl(), item.getCollectorLocalPath(), item.getPromotedOssUrl());
                String promotedKey = StringUtils.firstNonBlank(item.getDraftOssObjectKey(), item.getPromotedOssObjectKey());
                if (StringUtils.isBlank(promotedUrl)) {
                    throw new BusinessException("attachment evidence path missing");
                }
                HouseAttachment existingAttachment = findExistingHouseAttachment(house.getId(), item, promotedUrl);
                HouseAttachment houseAttachment = existingAttachment;
                if (houseAttachment == null) {
                    houseAttachment = new HouseAttachment();
                    houseAttachment.setUserId(house.getUserId());
                    houseAttachment.setHouseId(house.getId());
                    houseAttachment.setUsername(StringUtils.defaultString(reviewerName));
                    houseAttachment.setImageName(item.getFileName());
                    houseAttachment.setUrl(promotedUrl);
                    houseAttachment.setDescription(StringUtils.defaultString(request.getCommunity()) + "-" + StringUtils.defaultString(request.getRoomNumber()));
                    houseAttachment.setContentType(item.getContentType() == null ? -1 : item.getContentType());
                    houseAttachmentService.save(houseAttachment);
                }

                item.setPromotionStatus("PROMOTED");
                item.setPromotedHouseAttachmentId(houseAttachment.getId());
                item.setPromotedOssUrl(promotedUrl);
                item.setPromotedOssObjectKey(promotedKey);
                item.setCorrelationReason(item.getCorrelationReason());
                wechatDraftAttachmentService.updateById(item);
            } catch (Exception ex) {
                hasPromotionFailure = true;
                item.setPromotionStatus("FAILED");
                item.setCorrelationReason(StringUtils.abbreviate(ex.getMessage(), 1024));
                wechatDraftAttachmentService.updateById(item);
            }
        }
        return hasPromotionFailure;
    }

    private HouseAttachment findExistingHouseAttachment(Long houseId, WechatDraftAttachment item, String promotedUrl) {
        return houseAttachmentService.getOne(
                Wrappers.lambdaQuery(HouseAttachment.class)
                        .eq(HouseAttachment::getHouseId, houseId)
                        .eq(StringUtils.isNotBlank(item.getFileName()), HouseAttachment::getImageName, item.getFileName())
                        .eq(StringUtils.isNotBlank(promotedUrl), HouseAttachment::getUrl, promotedUrl)
                        .eq(HouseAttachment::getContentType, item.getContentType() == null ? -1 : item.getContentType()),
                false
        );
    }

    private WechatHouseDraft requireDraft(Long draftId) {
        WechatHouseDraft draft = this.getById(draftId);
        if (draft == null) {
            throw new BusinessException("wechat draft not found");
        }
        return draft;
    }

}
