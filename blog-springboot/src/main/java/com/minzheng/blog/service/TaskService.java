package com.minzheng.blog.service;

/**
 * 任务服务
 *
 * @author caiguoyu
 * @date 2022/9/27
 */
public interface TaskService {

    /**
     * 清除ip归属地缓存
     *
     * @author caiguoyu
     * @date 2022/9/27
     */
    void clearIpAddressSourceCache();

    /**
     * 清除匿名评论访客用户
     *
     * @author caiguoyu
     * @date 2022/9/27
     */
    void clearAnonymousVisitor();

    /**
     * 清除聊天室匿名信息
     *
     * @author caiguoyu
     * @date 2022/9/27
     */
    void clearIpAddressNickname();
}
