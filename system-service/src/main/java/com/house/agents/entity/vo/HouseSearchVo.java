package com.house.agents.entity.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.house.agents.Enum.FileContentTypeEnum;
import com.house.agents.Enum.SearchFileTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @Description:
 * @Author: Gavin
 * @Date: 7/29/2023 1:46 PM
 */
@Data
@Schema(description = "查询条件的对象")
public class HouseSearchVo {

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
    // ===
    @Schema(description = "小区名称")
    private String community;

    @Schema(description = "地铁线路")
    private String subway;

    @Schema(description = "地铁线路")
    private List<String> subways;

    @Schema(description = "楼栋及房间号")
    private String roomNumber;

    @Schema(description = "月租金的范围")
    private String rentRange;

    @Schema(description = "月租金起始")
    private BigDecimal startRent;

    @Schema(description = "结束月租金结束")
    private BigDecimal endRent;

    @Schema(description = "朝向")
    private String orientation;

    @Schema(description = "钥匙位置或密码")
    private String keyOrPassword;

    @Schema(description = "备注")
    private String remark;
    // ===
    @Schema(description = "创建用户的id")
    private Long userId;

    @Schema(description = "房子所属的房东的姓名")
    private String landlordName;

    @Schema(description = "房子的id")
    private String id;

    /**
     * @see SearchFileTypeEnum
     * 查询的时候需要查询的附件的类型
     */
    @Schema(description = "附件的类型")
    private int fileType;

    @Schema(description = "房子状态")
    private int houseStatus;
}
