package com.minzheng.blog.task;

import com.minzheng.blog.service.GameService;
import com.minzheng.blog.vo.SteamSyncVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Steam游戏同步任务
 */
@Component
@Slf4j
public class SteamGameSyncTask {

    @Resource
    private GameService gameService;

    @Scheduled(cron = "0 0 3 * * ?", zone = "Asia/Shanghai")
    public void syncSteamGames() {
        try {
            log.info("开始同步Steam游戏列表");
            gameService.syncSteamGames(SteamSyncVO.builder().includePlayedFreeGames(true).build());
            log.info("Steam游戏列表同步结束");
        } catch (Exception e) {
            log.warn("Steam游戏列表同步跳过：{}", e.getMessage());
        }
    }
}
