package com.minzheng.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minzheng.blog.dao.GameConfigDao;
import com.minzheng.blog.dto.GameConfigDTO;
import com.minzheng.blog.entity.GameConfig;
import com.minzheng.blog.service.GameConfigService;
import com.minzheng.blog.util.BeanCopyUtils;
import com.minzheng.blog.vo.GameConfigSaveVO;
import com.minzheng.blog.vo.GameConfigVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 游戏平台同步配置
 */
@Service
public class GameConfigServiceImpl extends ServiceImpl<GameConfigDao, GameConfig>
        implements GameConfigService {

    private static final LinkedHashSet<String> PLATFORM_SET = new LinkedHashSet<>(Arrays.asList(
            "STEAM", "PSN", "PC", "MOBILE", "SWITCH"
    ));

    @Override
    public List<GameConfigDTO> listGameConfigs() {
        List<GameConfig> configList = this.list(new LambdaQueryWrapper<GameConfig>()
                .orderByAsc(GameConfig::getPlatform)
                .orderByAsc(GameConfig::getSortOrder)
                .orderByAsc(GameConfig::getId));
        return BeanCopyUtils.copyList(configList, GameConfigDTO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveGameConfigs(GameConfigSaveVO saveVO) {
        List<GameConfigVO> configVOList = Objects.isNull(saveVO) || Objects.isNull(saveVO.getConfigList())
                ? Collections.emptyList()
                : saveVO.getConfigList();
        Map<String, GameConfig> configMap = new LinkedHashMap<>();
        for (int i = 0; i < configVOList.size(); i++) {
            GameConfigVO configVO = configVOList.get(i);
            if (Objects.isNull(configVO)
                    || !StringUtils.hasText(configVO.getPlatform())
                    || !StringUtils.hasText(configVO.getConfigKey())) {
                continue;
            }
            String platform = configVO.getPlatform().trim().toUpperCase();
            String configKey = configVO.getConfigKey().trim().toUpperCase();
            if (!PLATFORM_SET.contains(platform)) {
                continue;
            }
            configMap.put(platform + ":" + configKey, GameConfig.builder()
                    .platform(platform)
                    .configName(StringUtils.hasText(configVO.getConfigName())
                            ? configVO.getConfigName().trim() : configKey)
                    .configKey(configKey)
                    .configValue(configVO.getConfigValue())
                    .sortOrder(Objects.isNull(configVO.getSortOrder()) ? i + 1 : configVO.getSortOrder())
                    .build());
        }
        List<GameConfig> configList = new ArrayList<>(configMap.values());
        this.remove(new LambdaQueryWrapper<GameConfig>());
        if (!configList.isEmpty()) {
            this.saveBatch(configList, 100);
        }
    }

    @Override
    public Map<String, String> getPlatformConfig(String platform) {
        if (!StringUtils.hasText(platform)) {
            return Collections.emptyMap();
        }
        return this.list(new LambdaQueryWrapper<GameConfig>()
                        .eq(GameConfig::getPlatform, platform.trim().toUpperCase()))
                .stream()
                .filter(item -> StringUtils.hasText(item.getConfigKey()))
                .collect(Collectors.toMap(GameConfig::getConfigKey,
                        item -> Objects.toString(item.getConfigValue(), ""),
                        (left, right) -> right,
                        LinkedHashMap::new));
    }
}
