package com.house.agents.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public enum SearchHouseStatusEnum {
    DEFAULT(0,"默认,不区分状态"),
    HOUSE_UP(1, "上架"),
    HOUSE_DOWN(2, "下架");
    private Integer code;// 状态码
    private String desc;// 描述
}