package com.minzheng.blog.service.impl;

import com.minzheng.blog.constant.RedisPrefixConst;
import com.minzheng.blog.service.RedisService;
import com.minzheng.blog.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 任务服务
 *
 * @author caiguoyu
 * @date 2022/9/27
 */
@Service
@Slf4j
public class TaskServiceImpl implements TaskService {
    @Resource
    RedisService redisService;

    /**
     * 清除ip归属地缓存
     *
     * @author caiguoyu
     * @date 2022/9/27
     */
    @Override
    public void clearIpAddressSourceCache() {
        redisService.del(RedisPrefixConst.IP_ADDRESS_SOURCE);
    }

    /**
     * 清除匿名评论访客用户
     *
     * @author caiguoyu
     * @date 2022/9/27
     */
    @Override
    public void clearAnonymousVisitor() {
        redisService.del(RedisPrefixConst.ANONYMOUS_VISITOR);
    }

    /**
     * 清除聊天室匿名信息
     *
     * @author caiguoyu
     * @date 2022/9/27
     */
    @Override
    public void clearIpAddressNickname() {
        log.info("开始清除聊天室匿名信息");
        Map<String, Object> stringObjectMap = redisService.hGetAll(RedisPrefixConst.IP_ADDRESS_NICKNAME);
        int size = stringObjectMap.size();
        if (size == 0) {
            log.info("无匿名信息");
        } else {
            Boolean del = redisService.del(RedisPrefixConst.IP_ADDRESS_NICKNAME);
            if (del) {
                log.info("清除成功，清除信息：{}条", size);
            } else {
                log.info("清除失败");
            }
        }
    }
}
