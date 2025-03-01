package com.minzheng.blog.service;

import com.minzheng.blog.entity.UserChatHistory;

import java.util.List;

/**
 * 用户聊天历史服务接口
 *
 * @author caiguoyu
 * @date 2025/02/25
 */
public interface UserChatHistoryService {

    /**
     * 添加用户消息
     *
     * @param userIdentifier 用户标识
     * @param nickname 用户昵称
     * @param message 消息内容
     * @return 更新后的用户聊天历史
     */
    UserChatHistory addUserMessage(String userIdentifier, String nickname, String message);
    
    /**
     * 添加AI回复
     *
     * @param userIdentifier 用户标识
     * @param message AI回复内容
     * @return 更新后的用户聊天历史
     */
    UserChatHistory addAIMessage(String userIdentifier, String message);
    
    /**
     * 获取用户历史会话
     *
     * @param userIdentifier 用户标识
     * @return 用户聊天历史
     */
    UserChatHistory getUserHistory(String userIdentifier);

    /**
     * 合并多个用户的历史会话
     *
     * @param userIdentifiers 用户标识列表
     * @param maxMessagesPerUser 每个用户最大消息数
     * @return 合并后的消息列表
     */
    List<UserChatHistory.ChatMessage> mergeUserHistories(List<String> userIdentifiers, int maxMessagesPerUser);
    
    /**
     * 清理超过一定时间未活动的会话
     *
     * @param hourThreshold 小时阈值
     */
    void cleanupInactiveHistories(int hourThreshold);
} 