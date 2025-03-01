package com.minzheng.blog.service.impl;

import com.minzheng.blog.entity.UserChatHistory;
import com.minzheng.blog.service.UserChatHistoryService;
import com.minzheng.blog.service.VoiceAIService;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 火山引擎大模型服务实现
 *
 * @author caiguoyu
 * @date 2023/02/25
 */
@Slf4j
@Service
public class VoiceAIServiceImpl implements VoiceAIService {

    @Autowired
    private UserChatHistoryService userChatHistoryService;

    /**
     * 火山API密钥
     */
    @Value("${volcano.api.key:}")
    private String apiKey;

    /**
     * 火山API基础URL
     */
    @Value("${volcano.api.base-url:https://ark.cn-beijing.volces.com/api/v3}")
    private String baseUrl;

    /**
     * 火山API模型名称
     */
    @Value("${volcano.api.model:deepseek-r1-250120}")
    private String modelName;

    /**
     * 最大历史消息数
     */
    @Value("${volcano.chat.max-history:10}")
    private int maxHistoryMessages;
    
    /**
     * 每个用户的最大消息数（多用户合并时）
     */
    @Value("${volcano.chat.max-messages-per-user:5}")
    private int maxMessagesPerUser;

    /**
     * 模型系统提示词
     */
    @Value("${volcano.chat.system-prompt:你是一个友好的聊天助手，会根据聊天上下文回答用户问题。}")
    private String systemPrompt;
    
    /**
     * 多人对话系统提示词
     */
    @Value("${volcano.chat.multi-user-system-prompt:你是一个群聊助手，会根据多位用户的聊天上下文回答问题。请注意区分不同用户的发言，并尽可能回应所有相关的用户。}")
    private String multiUserSystemPrompt;

    /**
     * 火山引擎服务客户端
     */
    private ArkService arkService;

    /**
     * 初始化服务
     */
    @PostConstruct
    public void init() {
        log.info("正在初始化火山引擎大模型服务...");
        try {
            arkService = ArkService.builder()
                    .timeout(Duration.ofSeconds(1800))
                    .connectTimeout(Duration.ofSeconds(20))
                    .baseUrl(baseUrl)
                    .apiKey(apiKey)
                    .build();
            log.info("火山引擎大模型服务初始化成功");
        } catch (Exception e) {
            log.error("火山引擎大模型服务初始化失败", e);
        }
    }

    @Override
    public String chat(String content) {
        return chatWithHistory(null, content);
    }

    @Override
    public String chatWithHistory(String userIdentifier, String content) {
        if (arkService == null) {
            log.error("火山引擎大模型服务未初始化");
            return "系统错误：AI服务未初始化";
        }

        log.info("发送消息到火山引擎大模型 - 用户：{}，消息：{}", userIdentifier, content);
        try {
            // 创建消息列表
            final List<ChatMessage> messages = new ArrayList<>();
            
            // 添加系统提示语
            if (StringUtils.isNotEmpty(systemPrompt)) {
                final ChatMessage systemMessage = ChatMessage.builder()
                        .role(ChatMessageRole.SYSTEM)
                        .content(systemPrompt)
                        .build();
                messages.add(systemMessage);
            }
            
            // 添加历史消息（如果有用户标识）
            if (StringUtils.isNotEmpty(userIdentifier)) {
                UserChatHistory history = userChatHistoryService.getUserHistory(userIdentifier);
                if (history != null) {
                    List<UserChatHistory.ChatMessage> historyMessages = history.getRecentMessages(maxHistoryMessages);
                    for (UserChatHistory.ChatMessage historyMessage : historyMessages) {
                        ChatMessageRole role = "user".equals(historyMessage.getRole()) 
                                ? ChatMessageRole.USER 
                                : ChatMessageRole.ASSISTANT;
                        
                        ChatMessage chatMessage = ChatMessage.builder()
                                .role(role)
                                .content(historyMessage.getContent())
                                .build();
                        messages.add(chatMessage);
                    }
                }
            }
            
            // 添加当前消息
            final ChatMessage userMessage = ChatMessage.builder()
                    .role(ChatMessageRole.USER)
                    .content(content)
                    .build();
            messages.add(userMessage);

            // 创建请求
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(modelName)
                    .messages(messages)
                    .build();

            // 获取回复
            AtomicReference<String> responseContent = new AtomicReference<>("");
            
            // 使用流式响应
            arkService.streamChatCompletion(request)
                    .doOnError(e -> {
                        log.error("调用火山引擎大模型API出错", e);
                        responseContent.set("系统错误：" + e.getMessage());
                    })
                    .blockingForEach(delta -> {
                        if (!delta.getChoices().isEmpty()) {
                            Object contentObj = delta.getChoices().get(0).getMessage().getContent();
                            Object reasoningObj = delta.getChoices().get(0).getMessage().getReasoningContent();
                            
                            if (contentObj != null) {
                                String contentStr = contentObj.toString();
                                if (StringUtils.isNotEmpty(contentStr)) {
                                    responseContent.getAndUpdate(s -> s + contentStr);
                                }
                            } else if (reasoningObj != null) {
                                String reasoningStr = reasoningObj.toString();
                                if (StringUtils.isNotEmpty(reasoningStr)) {
                                    responseContent.getAndUpdate(s -> s + reasoningStr);
                                }
                            }
                        }
                    });
            
            return responseContent.get();
        } catch (Exception e) {
            log.error("调用火山引擎大模型API出错", e);
            return "系统错误：" + e.getMessage();
        }
    }
    
    @Override
    public String chatWithMultipleHistories(List<String> userIdentifiers, String content) {
        if (arkService == null) {
            log.error("火山引擎大模型服务未初始化");
            return "系统错误：AI服务未初始化";
        }

        log.info("发送多人对话消息到火山引擎大模型 - 用户数：{}，消息：{}", userIdentifiers.size(), content);
        try {
            // 创建消息列表
            final List<ChatMessage> messages = new ArrayList<>();
            
            // 添加多用户对话系统提示语
            if (StringUtils.isNotEmpty(multiUserSystemPrompt)) {
                final ChatMessage systemMessage = ChatMessage.builder()
                        .role(ChatMessageRole.SYSTEM)
                        .content(multiUserSystemPrompt)
                        .build();
                messages.add(systemMessage);
            }
            
            // 获取并合并多个用户的历史消息
            List<UserChatHistory.ChatMessage> mergedHistory = userChatHistoryService.mergeUserHistories(
                    userIdentifiers, maxMessagesPerUser);
            
            // 将合并后的消息添加到请求中
            for (UserChatHistory.ChatMessage historyMessage : mergedHistory) {
                ChatMessageRole role = "user".equals(historyMessage.getRole()) 
                        ? ChatMessageRole.USER 
                        : ChatMessageRole.ASSISTANT;
                
                ChatMessage chatMessage = ChatMessage.builder()
                        .role(role)
                        .content(historyMessage.getContent())
                        .build();
                messages.add(chatMessage);
            }
            
            // 添加当前消息
            final ChatMessage userMessage = ChatMessage.builder()
                    .role(ChatMessageRole.USER)
                    .content(content)
                    .build();
            messages.add(userMessage);

            // 创建请求
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(modelName)
                    .messages(messages)
                    .build();

            // 获取回复
            AtomicReference<String> responseContent = new AtomicReference<>("");
            
            // 使用流式响应
            arkService.streamChatCompletion(request)
                    .doOnError(e -> {
                        log.error("调用火山引擎大模型API出错", e);
                        responseContent.set("系统错误：" + e.getMessage());
                    })
                    .blockingForEach(delta -> {
                        if (!delta.getChoices().isEmpty()) {
                            Object contentObj = delta.getChoices().get(0).getMessage().getContent();
                            Object reasoningObj = delta.getChoices().get(0).getMessage().getReasoningContent();
                            
                            if (contentObj != null) {
                                String contentStr = contentObj.toString();
                                if (StringUtils.isNotEmpty(contentStr)) {
                                    responseContent.getAndUpdate(s -> s + contentStr);
                                }
                            } else if (reasoningObj != null) {
                                String reasoningStr = reasoningObj.toString();
                                if (StringUtils.isNotEmpty(reasoningStr)) {
                                    responseContent.getAndUpdate(s -> s + reasoningStr);
                                }
                            }
                        }
                    });
            
            return responseContent.get();
        } catch (Exception e) {
            log.error("调用火山引擎大模型API出错", e);
            return "系统错误：" + e.getMessage();
        }
    }
    
    /**
     * 定期清理不活跃的聊天历史（24小时未活动）
     */
    @Scheduled(cron = "0 0 */2 * * ?") // 每隔2小时执行一次
    public void cleanupInactiveHistories() {
        userChatHistoryService.cleanupInactiveHistories(24);
    }
} 