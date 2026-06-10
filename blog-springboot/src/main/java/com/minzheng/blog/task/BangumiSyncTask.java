package com.minzheng.blog.task;

import com.minzheng.blog.service.BangumiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * B站追番同步任务
 *
 * @author caiguoyu
 * @date 2026/06/09
 */
@Component
@Slf4j
public class BangumiSyncTask {

    @Resource
    private BangumiService bangumiService;

    /**
     * 每天凌晨2点同步B站追番
     */
    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Shanghai")
    public void syncBangumis() {
        log.info("开始同步B站追番列表");
        bangumiService.syncBangumis();
        log.info("B站追番列表同步结束");
    }

}
