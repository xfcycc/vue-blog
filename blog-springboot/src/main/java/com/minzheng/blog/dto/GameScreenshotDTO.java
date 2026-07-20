package com.minzheng.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公开游戏截图
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameScreenshotDTO {

    private String displayUrl;

    private Integer displayWidth;

    private Integer displayHeight;

    private String frameType;

    private Integer columnSpan;

    private Integer sortOrder;
}
