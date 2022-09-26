package com.minzheng.blog.task;

import com.minzheng.blog.constant.RedisPrefixConst;
import com.minzheng.blog.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author caiguoyu
 * @date 2022/9/26
 */
@Component
@Slf4j
public class ClearDataTask {

    /**
     * 该常量禁止用在其他类
     */
    private static final String LOCK = "monthly_clear_task_lock";

    @Resource
    RedisService redisService;

    /**
     * 每月最后一天凌晨4点执行
     *
     * @author caiguoyu
     * @date 2022/9/26
     */
    @Scheduled(cron = "0 0 4 L * ? ")
    public void monthlyClearTask() {
        if (redisService.singleLock(LOCK)) {
            return;
        }
        log.info("本月任务开始执行");
        // ip归属地信息
        redisService.del(RedisPrefixConst.IP_ADDRESS_SOURCE);
        // 匿名用户信息
        redisService.del(RedisPrefixConst.ANONYMOUS_VISITOR);
        log.info("本月任务执行结束");
    }
}
