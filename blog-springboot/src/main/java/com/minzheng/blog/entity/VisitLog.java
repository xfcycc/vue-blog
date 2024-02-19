package com.minzheng.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author caiguoyu
 * @date 2024/2/18
 */

/**
 * 访问日志表
 */
@ApiModel(description = "访问日志表")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "tb_visit_log")
public class VisitLog implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    private Long id;

    @TableField(value = "ip")
    @ApiModelProperty(value = "")
    private String ip;

    @TableField(value = "agent")
    @ApiModelProperty(value = "")
    private String agent;

    @TableField(value = "browser")
    @ApiModelProperty(value = "")
    private String browser;

    @TableField(value = "area")
    @ApiModelProperty(value = "")
    private String area;

    @TableField(value = "visit_time")
    @ApiModelProperty(value = "")
    private Date visitTime;

    private static final long serialVersionUID = 1L;
}