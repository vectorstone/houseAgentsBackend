package com.house.agents.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.house.agents.config.WechatIngestProperties;
import com.house.agents.entity.WechatDraftAttachment;
import com.house.agents.entity.WechatHouseDraft;
import com.house.agents.entity.SysUser;
import com.house.agents.entity.vo.WechatDraftApproveVo;
import com.house.agents.entity.vo.WechatDraftAttachmentStageVo;
import com.house.agents.entity.vo.WechatDraftIngestVo;
import com.house.agents.entity.vo.WechatDraftRejectVo;
import com.house.agents.entity.vo.WechatDraftUpdateVo;
import com.house.agents.result.R;
import com.house.agents.service.WechatDraftAttachmentService;
import com.house.agents.service.WechatHouseDraftService;
import com.house.agents.utils.WechatIngestAuthUtils;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang3.StringUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "微信房源草稿采集")
@RestController
@RequestMapping("/api/wechat-house-drafts")
@Transactional
public class WechatHouseDraftController {

    @Autowired
    private WechatHouseDraftService wechatHouseDraftService;

    @Autowired
    private WechatIngestProperties wechatIngestProperties;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WechatDraftAttachmentService wechatDraftAttachmentService;

    @Operation(summary = "采集器幂等写入微信房源草稿")
    @PostMapping("/ingest")
    public R ingest(
            @RequestHeader(value = "X-Collector-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(value = "X-Collector-Signature", required = false) String signature,
            @RequestBody WechatDraftIngestVo body,
            HttpServletRequest request
    ) throws Exception {
        String rawBody = objectMapper.writeValueAsString(body);
        boolean verified = WechatIngestAuthUtils.verify(
                request.getMethod(),
                request.getRequestURI(),
                timestamp,
                idempotencyKey,
                signature,
                rawBody,
                wechatIngestProperties,
                redisTemplate
        );
        if (!verified) {
            return R.error().code(HttpStatus.FORBIDDEN.value()).message("collector auth verify failed");
        }
        WechatHouseDraft draft = wechatHouseDraftService.ingestDraft(body);
        return R.ok().data("draftId", draft.getId()).data("sourceKey", draft.getSourceKey()).data("draftStatus", draft.getDraftStatus());
    }


    @PreAuthorize("hasAnyAuthority('bnt.wechatDraft.update')")
    @Operation(summary = "草稿附件 staging")
    @PostMapping("/{draftId}/attachments")
    public R stageAttachment(
            @PathVariable("draftId") Long draftId,
            @RequestBody WechatDraftAttachmentStageVo body,
            @RequestHeader("token") String token
    ) {
        validUser(token);
        wechatHouseDraftService.stageAttachment(draftId, body);
        return R.ok().data("draftId", draftId).data("attachmentSourceKey", body.getSourceKey());
    }

    @PreAuthorize("hasAnyAuthority('bnt.wechatDraft.approve')")
    @Operation(summary = "审批微信房源草稿并创建正式 House")
    @PostMapping("/{draftId}/approve")
    public R approveDraft(
            @PathVariable("draftId") Long draftId,
            @RequestBody WechatDraftApproveVo body,
            @RequestHeader("token") String token
    ) {
        SysUser sysUser = validUser(token);
        WechatHouseDraft draft = wechatHouseDraftService.approveDraft(draftId, body, sysUser.getId(), StringUtils.defaultIfBlank(sysUser.getName(), sysUser.getUsername()));
        return R.ok().data("draftId", draft.getId()).data("createdHouseId", draft.getCreatedHouseId()).data("draftStatus", draft.getDraftStatus());
    }

    @PreAuthorize("hasAnyAuthority('bnt.wechatDraft.reject')")
    @Operation(summary = "拒绝微信房源草稿")
    @PostMapping("/{draftId}/reject")
    public R rejectDraft(
            @PathVariable("draftId") Long draftId,
            @RequestBody WechatDraftRejectVo body,
            @RequestHeader("token") String token
    ) {
        SysUser sysUser = validUser(token);
        WechatHouseDraft draft = wechatHouseDraftService.rejectDraft(draftId, body, sysUser.getId());
        return R.ok().data("draftId", draft.getId()).data("draftStatus", draft.getDraftStatus());
    }

    @PreAuthorize("hasAnyAuthority('bnt.wechatDraft.list')")
    @Operation(summary = "草稿分页列表")
    @GetMapping("/page/{pageNum}/{pageSize}")
    public R list(
            @PathVariable("pageNum") Integer pageNum,
            @PathVariable("pageSize") Integer pageSize,
            @RequestHeader("token") String token
    ) {
        validUser(token);
        Page<WechatHouseDraft> page = wechatHouseDraftService.page(new Page<>(pageNum, pageSize));
        List<Map<String, Object>> items = page.getRecords().stream().map(this::toListItem).collect(Collectors.toList());
        Map<String, Object> pageData = new HashMap<>();
        pageData.put("records", items);
        pageData.put("total", page.getTotal());
        pageData.put("size", page.getSize());
        pageData.put("current", page.getCurrent());
        return R.ok().data("items", pageData);
    }

    @PreAuthorize("hasAnyAuthority('bnt.wechatDraft.view')")
    @Operation(summary = "草稿详情")
    @GetMapping("/{id}")
    public R detail(@PathVariable("id") Long id, @RequestHeader("token") String token) {
        validUser(token);
        WechatHouseDraft draft = wechatHouseDraftService.getById(id);
        if (draft == null) {
            return R.error().code(HttpStatus.NOT_FOUND.value()).message("wechat draft not found");
        }
        return R.ok().data(buildDetail(draft));
    }

    @PreAuthorize("hasAnyAuthority('bnt.wechatDraft.update')")
    @Operation(summary = "保存微信房源草稿")
    @PutMapping("/{draftId}")
    public R updateDraft(
            @PathVariable("draftId") Long draftId,
            @RequestBody WechatDraftUpdateVo body,
            @RequestHeader("token") String token
    ) {
        SysUser sysUser = validUser(token);
        WechatHouseDraft draft = wechatHouseDraftService.updateDraft(draftId, body, sysUser.getId());
        return R.ok().data("draftId", draft.getId()).data("draftStatus", draft.getDraftStatus());
    }
    private Map<String, Object> toListItem(WechatHouseDraft draft) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", draft.getId());
        item.put("sourceGroupName", draft.getSourceGroupName());
        item.put("senderDisplayName", draft.getSenderDisplayName());
        item.put("draftStatus", draft.getDraftStatus());
        item.put("correlationStatus", firstCorrelationStatus(draft.getId()));
        item.put("overallConfidence", draft.getOverallConfidence());
        item.put("visibleText", draft.getVisibleText());
        item.put("createdHouseId", draft.getCreatedHouseId());
        return item;
    }

    private Map<String, Object> buildDetail(WechatHouseDraft draft) {
        Map<String, Object> detail = toListItem(draft);
        detail.put("sourceGroupKey", draft.getSourceGroupKey());
        detail.put("sourcePlatform", draft.getSourcePlatform());
        detail.put("messageTime", draft.getMessageTime());
        detail.put("collectorReceiveTime", draft.getCollectorReceiveTime());
        detail.put("reviewNote", draft.getReviewNote());
        detail.put("failureReason", draft.getFailureReason());
        detail.put("form", parseJsonMap(draft.getExtractedJson()));
        detail.put("fieldConfidence", parseJsonMap(draft.getFieldConfidenceJson()));
        List<Map<String, Object>> evidences = new ArrayList<>();
        Map<String, Object> textEvidence = new HashMap<>();
        textEvidence.put("type", "text");
        textEvidence.put("content", draft.getVisibleText());
        textEvidence.put("confidence", draft.getOverallConfidence());
        evidences.add(textEvidence);
        List<WechatDraftAttachment> attachments = wechatDraftAttachmentService.list(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(WechatDraftAttachment.class)
                        .eq(WechatDraftAttachment::getDraftId, draft.getId())
        );
        for (WechatDraftAttachment attachment : attachments) {
            Map<String, Object> attachmentEvidence = new HashMap<>();
            attachmentEvidence.put("type", attachment.getMimeType() != null ? attachment.getMimeType() : "attachment");
            attachmentEvidence.put("content", StringUtils.firstNonBlank(attachment.getDraftOssUrl(), attachment.getCollectorLocalPath(), attachment.getFileName()));
            attachmentEvidence.put("confidence", attachment.getCorrelationScore());
            attachmentEvidence.put("correlationStatus", attachment.getCorrelationStatus());
            attachmentEvidence.put("promotionStatus", attachment.getPromotionStatus());
            evidences.add(attachmentEvidence);
        }
        detail.put("evidences", evidences);
        return detail;
    }

    private Map<String, Object> parseJsonMap(String json) {
        if (StringUtils.isBlank(json)) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("raw", json);
            return fallback;
        }
    }

    private String firstCorrelationStatus(Long draftId) {
        List<WechatDraftAttachment> attachments = wechatDraftAttachmentService.list(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(WechatDraftAttachment.class)
                        .eq(WechatDraftAttachment::getDraftId, draftId)
        );
        if (attachments == null || attachments.isEmpty()) {
            return "text_only";
        }
        return attachments.stream()
                .map(WechatDraftAttachment::getCorrelationStatus)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse("unknown");
    }

    private SysUser validUser(String token) {
        SysUser sysUser = (SysUser) redisTemplate.boundValueOps(token).get();
        if (sysUser == null) {
            throw new IllegalArgumentException("token user not found");
        }
        return sysUser;
    }

}
