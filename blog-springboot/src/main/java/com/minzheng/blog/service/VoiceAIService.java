package com.minzheng.blog.service;

import java.util.List;

/**
 * 火山引擎大模型服务接口
 *
 * @author caiguoyu
 * @date 2023/02/25
 */
public interface VoiceAIService {

    /**
     * 发送消息到大模型并获取回复
     *
     * @param content 用户输入内容
     * @return 大模型回复内容
     */
    String chat(String content);
    
    /**
     * 使用历史消息进行多轮对话
     *
     * @param userIdentifier 用户标识
     * @param content 用户输入内容
     * @return 大模型回复内容
     */
    String chatWithHistory(String userIdentifier, String content);
    
    /**
     * 合并多个用户历史进行对话
     *
     * @param userIdentifiers 用户标识列表
     * @param content 当前用户输入内容
     * @return 大模型回复内容
     */
    String chatWithMultipleHistories(List<String> userIdentifiers, String content);
} 