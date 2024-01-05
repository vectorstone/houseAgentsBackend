package com.house.agents.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.StringTypeHandler;
import org.apache.ibatis.type.TypeHandler;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="user_opt_log", description="保存用户操作日志的对象")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOptLog {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "用户id")
    @TableField(value = "user_id")
    private long userId;

    @ApiModelProperty(value = "用户名称")
    @TableField(value = "username")
    private String username;

    @ApiModelProperty(value = "用户ip地址")
    @TableField(value = "ip")
    private String ip;

    @ApiModelProperty(value = "用户的操作")
    @TableField(value = "operation")
    private String operation;

    @ApiModelProperty(value = "用户的请求参数")
    @TableField(value = "request",typeHandler = JacksonTypeHandler.class)
    private String request;

    @ApiModelProperty(value = "请求的响应结果")
    @TableField(value = "response",typeHandler = JacksonTypeHandler.class)
    private String response;

    @ApiModelProperty(value = "程序运行的时间")
    @TableField(value = "performanceTime")
    private String performanceTime;

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

}
