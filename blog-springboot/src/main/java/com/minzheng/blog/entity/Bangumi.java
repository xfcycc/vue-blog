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
 * B站追番
 *
 * @author caiguoyu
 * @date 2026/06/09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tb_bangumi")
public class Bangumi {

    /**
     * 剧集id
     */
    @TableId(value = "season_id", type = IdType.INPUT)
    private Integer seasonId;

    /**
     * 媒体id
     */
    private Long mediaId;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 标题
     */
    private String title;

    /**
     * 剧集类型
     */
    private Integer seasonType;

    /**
     * 剧集类型名称
     */
    private String seasonTypeName;

    /**
     * 封面
     */
    private String cover;

    /**
     * 方形封面
     */
    private String squareCover;

    /**
     * B站链接
     */
    private String url;

    /**
     * 观看进度
     */
    private String progress;

    /**
     * 最新集数
     */
    private String latestEpIndex;

    /**
     * 最新集标题
     */
    private String latestEpTitle;

    /**
     * 最新集长标题
     */
    private String latestEpLongTitle;

    /**
     * 最新集封面
     */
    private String latestEpCover;

    /**
     * 最新集发布时间
     */
    private LocalDateTime latestEpPubTime;

    /**
     * 评分
     */
    private BigDecimal ratingScore;

    /**
     * 评分人数
     */
    private Integer ratingCount;

    /**
     * 地区
     */
    private String area;

    /**
     * 风格
     */
    private String styles;

    /**
     * 是否完结
     */
    private Integer isFinish;

    /**
     * 是否可观看
     */
    private Integer canWatch;

    /**
     * 追番状态
     */
    private Integer followStatus;

    /**
     * 更新时间说明
     */
    private String renewalTime;

    /**
     * 上线时间
     */
    private LocalDateTime pubTime;

    /**
     * 首播日期
     */
    private String releaseDate;

    /**
     * 追番人数
     */
    private Long followCount;

    /**
     * 播放量
     */
    private Long viewCount;

    /**
     * 弹幕数
     */
    private Long danmakuCount;

    /**
     * 收藏数
     */
    private Long favoriteCount;

    /**
     * 简介
     */
    private String evaluate;

    /**
     * 加入时间
     */
    private LocalDateTime followTime;

    /**
     * 同步时间
     */
    private LocalDateTime syncTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

}
