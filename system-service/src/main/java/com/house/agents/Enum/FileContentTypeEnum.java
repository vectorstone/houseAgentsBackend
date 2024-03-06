package com.house.agents.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 *
 */
@Getter
@AllArgsConstructor
@ToString
public enum FileContentTypeEnum {
    HOUSE_IMAGE(0, "图片"),
    HOUSE_VIDEO(1, "视频"),
    HOUSE_MUSIC(2, "音乐"),
    HOUSE_CONTRACT(3, "合同"),
    HOUSE_OTHER(-1, "其他");
    private Integer code;//状态码
    private String desc;//描述
}


