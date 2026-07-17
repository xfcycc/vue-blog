package com.minzheng.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 保存游戏平台同步配置
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameConfigSaveVO {

    private List<GameConfigVO> configList;
}
