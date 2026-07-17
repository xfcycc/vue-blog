package com.minzheng.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 游戏平台同步配置项
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameConfigVO {

    private Integer id;

    private String platform;

    private String configName;

    private String configKey;

    private String configValue;

    private Integer sortOrder;
}
