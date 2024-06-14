package com.house.agents.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="SysMenu对象", description="菜单表")
public class SubWayDetail implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "名称")
    private String text;

    @ApiModelProperty(value = "数量")
    private int badge;

    @ApiModelProperty(value = "红点显示")
    private boolean dot;

    @ApiModelProperty(value = "是否禁用")
    private boolean disabled;

    @ApiModelProperty(value = "子节点")
    List<Subway> children;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    //设置自动填充的时机 指定新增时和更新时填充字段,我们这里新增的时候创建create和update时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    //设置自动填充的时机 指定新增时和更新时填充字段,我们这里新增的时候创建create和update时间
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    @ApiModelProperty(value = "删除标记（0:不可用 1:可用）")
    @TableField("is_deleted")
    @TableLogic
    private Boolean deleted;
}
