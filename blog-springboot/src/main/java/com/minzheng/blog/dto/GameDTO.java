package com.minzheng.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 前台游戏卡片
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameDTO {

    private Integer id;

    private String source;

    private String gameName;

    private String gameIntro;

    private String cover;

    private String gameUrl;

    private List<String> platformList;

    private List<String> tagList;

    private String playStatus;

    private BigDecimal personalScore;

    private Integer playtimeForever;

    private Integer playtimeTwoWeeks;

    private LocalDateTime lastPlayedTime;

    private List<GameFieldDTO> cardFieldList;
}
