package com.house.agents.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name="house", description="对应数据库中的房子表")
public class House implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "用户id")
    @TableField(value = "user_id")
    private long userId;

    @Schema(description = "小区名称")
    @TableField(value = "community")
    private String community;

    @Schema(description = "地铁线路")
    @TableField(value = "subway")
    private String subway;

    @Schema(description = "楼栋及房间号")
    @TableField(value = "room_number")
    private String roomNumber;

    @Schema(description = "月租金")
    @TableField(value = "rent")
    private BigDecimal rent;

    @Schema(description = "朝向")
    @TableField(value = "orientation")
    private String orientation;

    @Schema(description = "钥匙位置或密码")
    @TableField(value = "keyOrPassword")
    private String keyOrPassword;

    @Schema(description = "备注")
    @TableField(value = "remark")
    private String remark;

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
     * @see com.house.agents.Enum.HouseStatusEnum
     */
    @Schema(description = "房子状态")
    @TableField(value = "houseStatus")
    private int houseStatus;

    @Schema(description = "房子所属的附件url集合")
    @TableField(exist = false)
    private List<HouseAttachment> houseAttachment;

    @Schema(description = "房子所属的房东的姓名")
    // @TableField(exist = false)
    @TableField(value = "landlordName")
    private String landlordName;

    @Schema(description = "房子的首图")
    @TableField(exist = false)
    private String headImage;
}
