package com.minzheng.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台游戏列表
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameBackDTO {

    private Integer id;

    private String source;

    private String sourceGameId;

    private String gameName;

    private String cover;

    private List<String> platformList;

    private String playStatus;

    private BigDecimal personalScore;

    private Integer sortOrder;

    private Integer isVisible;

    private Integer playtimeForever;

    private LocalDateTime lastPlayedTime;

    private LocalDateTime syncTime;
}
