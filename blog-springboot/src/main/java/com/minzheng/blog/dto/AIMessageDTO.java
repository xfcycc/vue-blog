package com.minzheng.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI消息传输对象
 *
 * @author caiguoyu
 * @date 2025/02/25
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AIMessageDTO {
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 发送者IP地址
     */
    private String ipAddress;
    
    /**
     * IP来源
     */
    private String ipSource;
    
    /**
     * 发送者昵称
     */
    private String nickname;
    
    /**
     * 发送者头像
     */
    private String avatar;
    
    /**
     * 发送者用户ID（可能为空）
     */
    private Integer userId;
    
    /**
     * 被@用户的IP地址列表
     */
    private List<String> mentionedIpAddresses;
} 