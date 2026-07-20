package com.minzheng.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 游戏详情
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameDetailDTO {

    private Integer id;

    private String source;

    private String sourceGameId;

    private String gameName;

    private String gameAlias;

    private String gameIntro;

    private String cover;

    private String sourceCover;

    private String customCover;

    private String gameUrl;

    private List<String> platformList;

    private List<String> tagList;

    private List<String> screenshotList;

    private List<GameScreenshotDTO> screenshotItemList;

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

    private List<GameFieldDTO> fieldList;
}
