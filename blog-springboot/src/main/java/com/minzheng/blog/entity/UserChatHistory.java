package com.minzheng.blog.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户聊天历史记录实体
 *
 * @author caiguoyu
 * @date 2025/02/25
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserChatHistory {
    
    /**
     * 用户标识（使用ipAddress或userId）
     */
    private String userIdentifier;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;
    
    /**
     * 会话消息列表
     */
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();
    
    /**
     * 添加消息
     *
     * @param role 角色（用户/AI）
     * @param content 消息内容
     */
    public void addMessage(String role, String content) {
        ChatMessage message = ChatMessage.builder()
                .role(role)
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
        messages.add(message);
        this.lastUpdateTime = LocalDateTime.now();
    }
    
    /**
     * 获取最近N条消息
     *
     * @param count 消息数量
     * @return 消息列表
     */
    public List<ChatMessage> getRecentMessages(int count) {
        int startIndex = Math.max(0, messages.size() - count);
        return messages.subList(startIndex, messages.size());
    }
    
    /**
     * 聊天消息实体
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatMessage {
        /**
         * 角色（user/assistant）
         */
        private String role;
        
        /**
         * 消息内容
         */
        private String content;
        
        /**
         * 消息时间
         */
        private LocalDateTime timestamp;
    }
} 