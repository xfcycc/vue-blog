package com.minzheng.blog.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台游戏截图
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GameScreenshotBackDTO extends GameScreenshotDTO {

    private String originalUrl;

    private Integer originalWidth;

    private Integer originalHeight;
}
