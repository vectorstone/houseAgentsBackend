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

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author Gavin
 * @since 2023-08-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="SysUser对象", description="用户表")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "会员id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "微信code")
    private String openid;

    @ApiModelProperty(value = "姓名")
    private String name;

    @ApiModelProperty(value = "手机")
    private String phone;

    @ApiModelProperty(value = "头像地址")
    private String headUrl;

    @ApiModelProperty(value = "密码盐")
    private String salt;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "状态（1：正常 0：停用）")
    private Integer status;

    //identifier
    @ApiModelProperty(value = "identifier",name = "用户身份标识")
    private Integer identifier;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    //设置自动填充的时机 指定新增时和更新时填充字段,我们这里新增的时候创建create和update时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date createTime;
    // 这个按键的配置让我真的是有点用不习惯,为什么这个按键的设置是这个样子的,为什么不能是正常一点的按键的设置,这个fn的按键的位置为什么是在最右边的位置,为什么不能是在左边的位置
    // 这样让我debug的时候怎么方便的进行debug,感觉是真的有点奇怪,但是这个键盘的按键的布局说实话还是比较的新颖的,最起码,没有明显的controller按键了,看着就比较的有意思

    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    //设置自动填充的时机 指定新增时和更新时填充字段,我们这里只让更新的时候自动填充,插入的时候
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    @ApiModelProperty(value = "删除标记（0:可用 1:已删除）")
    @TableLogic
    private Integer isDeleted;

    @ApiModelProperty("用户的角色列表")
    @TableField(exist = false)
    private List<SysRole> roleList;

    @ApiModelProperty("用户的权限集合")
    @TableField(exist = false)
    List<String> userPermsList;



}
