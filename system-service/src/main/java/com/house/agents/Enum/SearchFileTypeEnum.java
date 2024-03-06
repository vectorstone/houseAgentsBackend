package com.house.agents.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public enum SearchFileTypeEnum {
    DEFAULT(0, "不拦截"),
    NOT_EMPTY(1, "视频或图片"),
    EMPTY(2, "没有附件");
    private int code;// 状态码
    private String desc;// 描述
}
