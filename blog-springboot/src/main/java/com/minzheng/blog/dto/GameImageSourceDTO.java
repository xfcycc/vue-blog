package com.minzheng.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 游戏图片源文件
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameImageSourceDTO {

    private String dataUrl;

    private String contentType;

    private Integer width;

    private Integer height;
}
