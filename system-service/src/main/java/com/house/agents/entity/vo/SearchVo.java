package com.house.agents.entity.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description:
 * @Author: Gavin
 * @Date: 7/29/2023 1:46 PM
 */
@Data
@Schema(description = "查询条件的对象")
public class SearchVo {

    @Schema(description = "起始日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    //设置自动填充的时机 指定新增时和更新时填充字段,我们这里只让更新的时候自动填充,插入的时候
    @TableField(fill = FieldFill.INSERT)
    private Date startTime;

    @Schema(description = "结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    //设置自动填充的时机 指定新增时和更新时填充字段,我们这里只让更新的时候自动填充,插入的时候
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date endTime;

    @Schema(description = "收支类型")
    private String type;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "类别")
    private String category;

    @Schema(description = "子类")
    @TableField("subCategory")
    private String subcategory;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "标签")
    private String label;

    @Schema(description = "创建用户的id")
    @TableId(value = "user", type = IdType.ASSIGN_ID)
    private Long user;
}
