package com.house.agents.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 数据字典
 * </p>
 *
 * @author Gavin
 * @since 2023-07-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name="地铁线路对象", description="地铁线路对象")
public class Subway implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
      @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "名称")
    private String text;

    @Schema(description = "值")
    private boolean disabled;


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

    @Schema(description = "删除标记（0:不可用 1:可用）")
    @TableField("is_deleted")
    @TableLogic
    private Boolean deleted;

}
