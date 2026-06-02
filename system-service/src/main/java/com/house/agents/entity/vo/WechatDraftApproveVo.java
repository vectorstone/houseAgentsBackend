package com.house.agents.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WechatDraftApproveVo {
    private Long targetUserId;
    private String community;
    private String subway;
    private String roomNumber;
    private BigDecimal rent;
    private String orientation;
    private String keyOrPassword;
    private String remark;
    private String landlordName;
    private Integer houseStatus;
    private String reviewNote;
}
