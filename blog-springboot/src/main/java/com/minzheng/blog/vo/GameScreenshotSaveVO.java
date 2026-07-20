package com.minzheng.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 保存游戏截图
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameScreenshotSaveVO {

    private String originalUrl;

    private String displayUrl;

    private Integer originalWidth;

    private Integer originalHeight;

    private Integer displayWidth;

    private Integer displayHeight;

    private String frameType;

    private Integer columnSpan;

    private Integer sortOrder;
}
