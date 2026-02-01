package com.house.agents.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name="HouseAttachment", description="对应数据库中的房子的附件信息")
public class HouseAttachment {
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "用户id")
    @TableField(value = "user_id")
    private long userId;

    @Schema(description = "房子id")
    @TableField(value = "house_id")
    private long houseId;

    @Schema(description = "房东的昵称")
    @TableField("username")
    private String username;

    @Schema(description = "图片或视频的名称")
    @TableField("image_name")
    private String imageName;

    @Schema(description = "地址")
    @TableField("url")
    private String url;

    @Schema(description = "描述")
    @TableField("description")
    private String description;

    @Schema(description = "创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    //设置自动填充的时机 指定新增时和更新时填充字段,我们这里只让更新的时候自动填充,插入的时候
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @Schema(description = "创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    //设置自动填充的时机 指定新增时和更新时填充字段,这里新增或者更新的时候自动填充,插入的时候
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @Schema(description = "删除标记(0:不可用 1:可用)")
    @TableField(value = "is_deleted")
    @TableLogic
    private Boolean deleted;

    /**
     * @see com.house.agents.Enum.FileContentTypeEnum
     */
    @Schema(description = "附件类型")
    @TableField("contentType")
    private int contentType;

}
