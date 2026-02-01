package com.house.agents.entity.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author Gavin
 * @since 2023-08-03
 */
@Data
@Schema(name="userVo", description="用户查询vo类")
public class UserVo {
    private String keyword;
    private String createTimeBegin;
    private String createTimeEnd;
}
