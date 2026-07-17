package com.minzheng.blog.service;

import com.minzheng.blog.dto.GameOneTimeSyncDTO;
import com.minzheng.blog.vo.PsnOneTimeSyncVO;

/**
 * 游戏一次性同步
 */
public interface GameOneTimeSyncService {

    GameOneTimeSyncDTO syncPsnGames(PsnOneTimeSyncVO syncVO);
}
