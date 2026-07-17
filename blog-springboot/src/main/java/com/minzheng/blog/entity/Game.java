package com.minzheng.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 游戏档案
 *
 * @author caiguoyu
 * @date 2026/07/17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_game")
public class Game {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String source;

    private String sourceGameId;

    private String gameName;

    private String gameAlias;

    private String gameIntro;

    private String sourceCover;

    private String customCover;

    private String gameUrl;

    private String platforms;

    private String tags;

    private String screenshots;

    private String screenshotLayout;

    private String playStatus;

    private BigDecimal personalScore;

    private Integer sortOrder;

    private Integer isVisible;

    private Integer playtimeForever;

    private Integer playtimeTwoWeeks;

    private LocalDateTime lastPlayedTime;

    private String reviewContent;

    private LocalDateTime syncTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
