package com.house.agents.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name="SysRoleMenu对象", description="角色菜单")
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "ShareToHouse")
public class ShareToHouse {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(value = "shareId")
    private Long shareId;

    @TableField(value = "houseId")
    private Long houseId;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    //设置自动填充的时机 指定新增时和更新时填充字段,我们这里新增的时候创建create和update时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    //设置自动填充的时机 指定新增时和更新时填充字段,我们这里新增的时候创建create和update时间
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    @Schema(description = "删除标记（0:可用 1:已删除）")
    @TableLogic
    private Integer isDeleted;
}
