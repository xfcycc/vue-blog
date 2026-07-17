package com.minzheng.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 保存游戏档案
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameSaveVO {

    private Integer id;

    private String gameName;

    private String gameAlias;

    private String gameIntro;

    private String customCover;

    private String gameUrl;

    private List<String> platformList;

    private List<String> tagList;

    private List<String> screenshotList;

    private String screenshotLayout;

    private String playStatus;

    private BigDecimal personalScore;

    private Integer sortOrder;

    private Integer isVisible;

    private Integer playtimeForever;

    private Integer playtimeTwoWeeks;

    private String lastPlayedTime;

    private String reviewContent;

    private List<GameFieldVO> fieldList;
}
