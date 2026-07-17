package com.minzheng.blog.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minzheng.blog.entity.GameConfig;
import org.springframework.stereotype.Repository;

/**
 * 游戏平台同步配置
 */
@Repository
public interface GameConfigDao extends BaseMapper<GameConfig> {
}
