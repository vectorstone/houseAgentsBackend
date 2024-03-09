package com.house.agents.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public enum HouseStatusEnum {
    HOUSE_UP(0, "上架"),
    HOUSE_DOWN(1, "下架");
    private Integer code;//状态码
    private String desc;//描述
}


