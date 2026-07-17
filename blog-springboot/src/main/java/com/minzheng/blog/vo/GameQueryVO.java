package com.minzheng.blog.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 前台游戏查询
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "前台游戏查询")
public class GameQueryVO {

    private Long current;

    private Long size;

    private String keywords;

    private String playStatus;

    private String platform;
}
