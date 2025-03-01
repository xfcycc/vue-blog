package com.minzheng.blog.service.impl;

import com.minzheng.blog.entity.UserChatHistory;
import com.minzheng.blog.service.UserChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 用户聊天历史服务实现
 *
 * @author caiguoyu
 * @date 2025/02/25
 */
@Slf4j
@Service
public class UserChatHistoryServiceImpl implements UserChatHistoryService {

    /**
     * 用户聊天历史缓存
     * 键：用户标识（IP或userId）
     * 值：用户聊天历史
     */
    private final Map<String, UserChatHistory> userHistoryCache = new ConcurrentHashMap<>();

    /**
     * 每个用户默认的历史消息保留数量
     */
    private static final int DEFAULT_HISTORY_SIZE = 10;

    @Override
    public UserChatHistory addUserMessage(String userIdentifier, String nickname, String message) {
        UserChatHistory history = getUserHistory(userIdentifier);
        
        // 如果没有历史记录，创建一个新的
        if (history == null) {
            history = UserChatHistory.builder()
                    .userIdentifier(userIdentifier)
                    .nickname(nickname)
                    .lastUpdateTime(LocalDateTime.now())
                    .messages(new ArrayList<>())
                    .build();
        } else {
            // 更新昵称和最后更新时间
            history.setNickname(nickname);
            history.setLastUpdateTime(LocalDateTime.now());
        }
        
        // 添加用户消息
        history.addMessage("user", message);
        
        // 更新缓存
        userHistoryCache.put(userIdentifier, history);
        
        log.info("添加用户消息 - 用户：{}，消息内容：{}", userIdentifier, message);
        return history;
    }

    @Override
    public UserChatHistory addAIMessage(String userIdentifier, String message) {
        UserChatHistory history = getUserHistory(userIdentifier);
        
        // 如果没有历史记录（这种情况不应该发生），记录错误
        if (history == null) {
            log.error("尝试添加AI消息，但未找到用户历史记录 - 用户：{}", userIdentifier);
            return null;
        }
        
        // 添加AI消息
        history.addMessage("assistant", message);
        
        // 更新最后更新时间
        history.setLastUpdateTime(LocalDateTime.now());
        
        // 更新缓存
        userHistoryCache.put(userIdentifier, history);
        
        log.info("添加AI消息 - 用户：{}，消息内容：{}", userIdentifier, message);
        return history;
    }

    @Override
    public UserChatHistory getUserHistory(String userIdentifier) {
        return userHistoryCache.get(userIdentifier);
    }

    @Override
    public List<UserChatHistory.ChatMessage> mergeUserHistories(List<String> userIdentifiers, int maxMessagesPerUser) {
        // 如果未指定最大消息数，使用默认值
        if (maxMessagesPerUser <= 0) {
            maxMessagesPerUser = DEFAULT_HISTORY_SIZE;
        }
        
        List<UserChatHistory.ChatMessage> allMessages = new ArrayList<>();
        
        // 获取每个用户的历史消息
        for (String userIdentifier : userIdentifiers) {
            UserChatHistory history = getUserHistory(userIdentifier);
            if (history != null) {
                // 获取该用户的最近消息
                List<UserChatHistory.ChatMessage> userMessages = history.getRecentMessages(maxMessagesPerUser);
                allMessages.addAll(userMessages);
            }
        }
        
        // 按时间排序
        allMessages.sort(Comparator.comparing(UserChatHistory.ChatMessage::getTimestamp));
        
        log.info("合并用户历史 - 用户数：{}，合并后消息数：{}", userIdentifiers.size(), allMessages.size());
        return allMessages;
    }

    @Override
    public void cleanupInactiveHistories(int hourThreshold) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(hourThreshold);
        
        List<String> keysToRemove = userHistoryCache.entrySet().stream()
                .filter(entry -> entry.getValue().getLastUpdateTime().isBefore(threshold))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        // 移除不活跃的历史记录
        keysToRemove.forEach(userHistoryCache::remove);
        
        log.info("清理不活跃聊天历史 - 清理数量：{}", keysToRemove.size());
    }
} 