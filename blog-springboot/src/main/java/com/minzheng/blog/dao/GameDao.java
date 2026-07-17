package com.minzheng.blog.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minzheng.blog.entity.Game;
import org.springframework.stereotype.Repository;

/**
 * 游戏档案
 *
 * @author caiguoyu
 * @date 2026/07/17
 */
@Repository
public interface GameDao extends BaseMapper<Game> {
}
