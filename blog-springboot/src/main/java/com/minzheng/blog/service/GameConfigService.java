package com.minzheng.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.minzheng.blog.dto.GameConfigDTO;
import com.minzheng.blog.entity.GameConfig;
import com.minzheng.blog.vo.GameConfigSaveVO;

import java.util.List;
import java.util.Map;

/**
 * 游戏平台同步配置
 */
public interface GameConfigService extends IService<GameConfig> {

    List<GameConfigDTO> listGameConfigs();

    void saveGameConfigs(GameConfigSaveVO saveVO);

    Map<String, String> getPlatformConfig(String platform);
}
