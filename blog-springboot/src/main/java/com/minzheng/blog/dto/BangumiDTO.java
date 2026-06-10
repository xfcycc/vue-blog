package com.minzheng.blog.dto;

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
public class BangumiDTO {

    /**
     * 剧集id
     */
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
     * 首播日期
     */
    private String releaseDate;

    /**
     * 追番人数
     */
    private Long followCount;

    /**
     * 简介
     */
    private String evaluate;

    /**
     * 同步时间
     */
    private LocalDateTime syncTime;

}
