package com.minzheng.blog.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minzheng.blog.entity.GameScreenshot;
import org.springframework.stereotype.Repository;

/**
 * 游戏截图数据访问
 */
@Repository
public interface GameScreenshotDao extends BaseMapper<GameScreenshot> {
}
