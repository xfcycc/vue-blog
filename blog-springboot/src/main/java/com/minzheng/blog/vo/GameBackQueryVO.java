package com.minzheng.blog.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 后台游戏查询
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "后台游戏查询")
public class GameBackQueryVO {

    private Long current;

    private Long size;

    private String keywords;

    private String source;

    private String platform;

    private String playStatus;

    private Integer isVisible;
}
