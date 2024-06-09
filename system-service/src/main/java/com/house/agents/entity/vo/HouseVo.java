package com.house.agents.entity.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
/**
 * <p>
 * 
 * </p>
 *
 * @author Gavin
 * @since 2023-07-28
 */
@Data
@ApiModel("对应数据库中的房子表")
public class HouseVo implements Serializable {

    @ApiModelProperty(value = "id")
    @ExcelProperty(index = 0,value = "id")
    // private Long id;
    private String id;

    @ApiModelProperty(value = "用户id")
    @ExcelProperty(index = 1,value = "用户id")
    // private long userId;
    private String userId;

    @ApiModelProperty(value = "小区名称")
    @ExcelProperty(index = 2,value = "小区名称")
    private String community;

    @ApiModelProperty(value = "地铁线路")
    @ExcelProperty(index = 3,value = "地铁线路")
    private String subway;

    @ApiModelProperty(value = "楼栋及房间号")
    @ExcelProperty(index = 4,value = "楼栋及房间号")
    private String roomNumber;

    @ApiModelProperty(value = "月租金")
    @ExcelProperty(index = 5,value = "月租金")
    private BigDecimal rent;

    @ApiModelProperty(value = "朝向")
    @ExcelProperty(index = 6,value = "朝向")
    private String orientation;

    @ApiModelProperty(value = "钥匙位置或密码")
    @ExcelProperty(index = 7,value = "钥匙位置或密码")
    private String keyOrPassword;

    @ApiModelProperty(value = "备注")
    @ExcelProperty(index = 8,value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建日期")
    @ExcelProperty(index = 9,value = "创建日期")
    private Date createTime;

    @ApiModelProperty(value = "修改日期")
    @ExcelProperty(index = 10,value = "修改日期")
    private Date updateTime;

    @ApiModelProperty(value = "房子状态")
    @ExcelProperty(index = 11,value= "房子状态")
    private int houseStatus;

    @ApiModelProperty(value = "房子所属的房东的姓名")
    @ExcelProperty(index =12,value = "房子所属的房东的姓名")
    private String landlordName;
}
