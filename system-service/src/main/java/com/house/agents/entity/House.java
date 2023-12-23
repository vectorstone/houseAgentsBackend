package com.house.agents.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 
 * </p>
 *
 * @author Gavin
 * @since 2023-07-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="house对象", description="对应数据库中的房子表")
public class House implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "用户id")
    @TableField(value = "user_id")
    private long userId;

    @ApiModelProperty(value = "小区名称")
    @TableField(value = "community")
    private String community;

    @ApiModelProperty(value = "地铁线路")
    @TableField(value = "subway")
    private String subway;

    @ApiModelProperty(value = "楼栋及房间号")
    @TableField(value = "room_number")
    private String roomNumber;

    @ApiModelProperty(value = "月租金")
    @TableField(value = "rent")
    private BigDecimal rent;

    @ApiModelProperty(value = "朝向")
    @TableField(value = "orientation")
    private String orientation;

    @ApiModelProperty(value = "钥匙位置或密码")
    @TableField(value = "keyOrPassword")
    private String keyOrPassword;

    @ApiModelProperty(value = "备注")
    @TableField(value = "remark")
    private String remark;

    @ApiModelProperty(value = "创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    //设置自动填充的时机 指定新增时和更新时填充字段,我们这里只让更新的时候自动填充,插入的时候
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    //设置自动填充的时机 指定新增时和更新时填充字段,这里新增或者更新的时候自动填充,插入的时候
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @ApiModelProperty(value = "删除标记(0:不可用 1:可用)")
    @TableField(value = "is_deleted")
    @TableLogic
    private Boolean deleted;

    @ApiModelProperty(value = "房子所属的附件url集合")
    @TableField(exist = false)
    private List<HouseAttachment> houseAttachment;
}
