package com.minzheng.blog.task;

import com.minzheng.blog.constant.RedisPrefixConst;
import com.minzheng.blog.service.RedisService;
import com.minzheng.blog.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author caiguoyu
 * @date 2022/9/26
 */
@Component
@Slf4j
public class ClearDataTask {
    //以下常量禁止用在其他类
    /**
     * 月度任务锁
     */
    private static final String LOCK_MONTH = "monthly_clear_task_lock";

    /**
     * 每日任务锁
     */
    private static final String LOCK_DAY = "monthly_clear_task_lock";

    @Resource
    RedisService redisService;

    @Resource
    TaskService taskService;

    /**
     * 每月最后一天凌晨4点执行
     *
     * @author caiguoyu
     * @date 2022/9/26
     */
    @Scheduled(cron = "0 0 4 L * ?")
    public void monthlyClearTask() {
        if (redisService.singleLock(LOCK_MONTH)) {
            return;
        }
        log.info("本月任务开始执行");
        // ip归属地信息
        taskService.clearIpAddressSourceCache();
        // 评论匿名用户信息
        taskService.clearAnonymousVisitor();
        log.info("本月任务执行结束");
    }

    /**
     * 每日凌晨4点执行
     *
     * @author caiguoyu
     * @date 2022/9/27
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void dailyClearTask() {
        if (redisService.singleLock(LOCK_DAY)) {
            return;
        }
        log.info("每日任务开始执行");
        // 清除聊天室匿名信息
        taskService.clearIpAddressNickname();
        log.info("每日任务执行结束");
    }
}
