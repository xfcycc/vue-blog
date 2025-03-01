package com.minzheng.blog.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.minzheng.blog.dictionary.NicknameDictionary;
import com.minzheng.blog.dao.ChatRecordDao;
import com.minzheng.blog.dto.AIMessageDTO;
import com.minzheng.blog.dto.ChatRecordDTO;
import com.minzheng.blog.dto.RecallMessageDTO;
import com.minzheng.blog.dto.WebsocketMessageDTO;
import com.minzheng.blog.entity.ChatRecord;
import com.minzheng.blog.enums.FilePathEnum;
import com.minzheng.blog.service.ChatRoomService;
import com.minzheng.blog.service.UserChatHistoryService;
import com.minzheng.blog.service.VoiceAIService;
import com.minzheng.blog.strategy.context.UploadStrategyContext;
import com.minzheng.blog.util.*;
import com.minzheng.blog.vo.VoiceVO;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.minzheng.blog.enums.ChatTypeEnum.*;

/**
 * websocket服务
 *
 * @author yezhiqiu
 * @date 2021/07/28
 */
@Data
@Service
@ServerEndpoint(value = "/websocket", configurator = WebSocketServiceImpl.ChatConfigurator.class)
public class WebSocketServiceImpl {

    /**
     * 用户session
     */
    private Session session;

    /**
     * 用户session集合
     */
    private static CopyOnWriteArraySet<WebSocketServiceImpl> webSocketSet = new CopyOnWriteArraySet<>();

    @Resource
    public void setChatRecordDao(ChatRecordDao chatRecordDao) {
        WebSocketServiceImpl.chatRecordDao = chatRecordDao;
    }

    @Resource
    public void setUploadStrategyContext(UploadStrategyContext uploadStrategyContext) {
        WebSocketServiceImpl.uploadStrategyContext = uploadStrategyContext;
    }

    @Resource
    public void setChatRoomService(ChatRoomService chatRoomService) {
        WebSocketServiceImpl.chatRoomService = chatRoomService;
    }
    
    @Resource
    public void setVoiceAIService(VoiceAIService voiceAIService) {
        WebSocketServiceImpl.voiceAIService = voiceAIService;
    }
    
    @Resource
    public void setUserChatHistoryService(UserChatHistoryService userChatHistoryService) {
        WebSocketServiceImpl.userChatHistoryService = userChatHistoryService;
    }

    private static ChatRoomService chatRoomService;

    private static ChatRecordDao chatRecordDao;

    private static UploadStrategyContext uploadStrategyContext;
    
    private static VoiceAIService voiceAIService;
    
    private static UserChatHistoryService userChatHistoryService;

    /**
     * 获取客户端真实ip
     */
    public static class ChatConfigurator extends ServerEndpointConfig.Configurator {

        public static String HEADER_NAME = "X-Real-IP";

        @Override
        public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
            try {
                String firstFoundHeader = request.getHeaders().get(HEADER_NAME.toLowerCase()).get(0);
                sec.getUserProperties().put(HEADER_NAME, firstFoundHeader);
            } catch (Exception e) {
                sec.getUserProperties().put(HEADER_NAME, "未知ip");
            }
        }
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) throws IOException {
        // 加入连接
        this.session = session;
        webSocketSet.add(this);
        // 更新在线人数
        updateOnlineCount();
        // 加载历史聊天记录
        ChatRecordDTO chatRecordDTO = listChartRecords(endpointConfig);
        // 发送消息
        WebsocketMessageDTO messageDTO = WebsocketMessageDTO.builder()
                .type(HISTORY_RECORD.getType())
                .data(chatRecordDTO)
                .build();
        synchronized (session) {
            session.getBasicRemote().sendText(JSON.toJSONString(messageDTO));
        }
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        WebsocketMessageDTO messageDTO = JSON.parseObject(message, WebsocketMessageDTO.class);
        switch (Objects.requireNonNull(getChatType(messageDTO.getType()))) {
            case SEND_MESSAGE:
                // 发送消息
                ChatRecord chatRecord = JSON.parseObject(JSON.toJSONString(messageDTO.getData()), ChatRecord.class);
                // 过滤html标签
                chatRecord.setContent(HTMLUtils.filter(chatRecord.getContent()));
                // 替换用户昵称
                replaceNickname(chatRecord);
                chatRecordDao.insert(chatRecord);
                messageDTO.setData(chatRecord);
                // 广播消息
                broadcastMessage(messageDTO);
                break;
            case RECALL_MESSAGE:
                // 撤回消息
                RecallMessageDTO recallMessage = JSON.parseObject(JSON.toJSONString(messageDTO.getData()), RecallMessageDTO.class);
                // 删除记录
                chatRecordDao.deleteById(recallMessage.getId());
                // 广播消息
                broadcastMessage(messageDTO);
                break;
            case HEART_BEAT:
                // 心跳消息
                messageDTO.setData("pong");
                session.getBasicRemote().sendText(JSON.toJSONString(messageDTO));
                break;
            case AI_MESSAGE:
                // 处理AI消息
                handleAIMessage(messageDTO, session);
                break;
            default:
                break;
        }
    }

    /**
     * 处理AI消息
     *
     * @param messageDTO 消息DTO
     * @param session 会话
     * @throws IOException IO异常
     * @author caiguoyu
     * @date 2023/02/25
     */
    private void handleAIMessage(WebsocketMessageDTO messageDTO, Session session) throws IOException {
        // 解析消息对象
        AIMessageDTO aiMessageDTO = JSON.parseObject(JSON.toJSONString(messageDTO.getData()), AIMessageDTO.class);
        
        // 过滤HTML标签
        String userContent = HTMLUtils.filter(aiMessageDTO.getContent());
        
        // 创建用户聊天记录
        ChatRecord userChatRecord = ChatRecord.builder()
                .userId(aiMessageDTO.getUserId())
                .nickname(aiMessageDTO.getNickname())
                .avatar(aiMessageDTO.getAvatar())
                .content(userContent)
                .type(SEND_MESSAGE.getType())
                .ipAddress(aiMessageDTO.getIpAddress())
                .ipSource(aiMessageDTO.getIpSource())
                .build();
        
        // 保存用户消息记录
        chatRecordDao.insert(userChatRecord);
        
        // 广播用户消息到所有客户端
        WebsocketMessageDTO userMessageDTO = WebsocketMessageDTO.builder()
                .type(SEND_MESSAGE.getType())
                .data(userChatRecord)
                .build();
        broadcastMessage(userMessageDTO);
        
        // 将用户消息添加到聊天历史
        userChatHistoryService.addUserMessage(
                aiMessageDTO.getIpAddress(),
                aiMessageDTO.getNickname(),
                userContent);
        
        // 创建要处理的用户标识列表（首先添加当前发送消息的用户）
        List<String> userIdsToProcess = new ArrayList<>();
        userIdsToProcess.add(aiMessageDTO.getIpAddress());
        
        // 检查是否有@用户，如果有，则将这些用户加入到处理列表
        if (aiMessageDTO.getMentionedIpAddresses() != null && !aiMessageDTO.getMentionedIpAddresses().isEmpty()) {
            userIdsToProcess.addAll(aiMessageDTO.getMentionedIpAddresses());
        }
        
        // 获取AI回复（多人或单人）
        String aiResponse;
        if (userIdsToProcess.size() > 1) {
            // 如果有多个用户，用合并历史的方式获取回复
            aiResponse = voiceAIService.chatWithMultipleHistories(userIdsToProcess, userContent);
        } else {
            // 如果只有一个用户，用单用户历史的方式获取回复
            aiResponse = voiceAIService.chatWithHistory(aiMessageDTO.getIpAddress(), userContent);
        }
        
        // 为每个处理的用户添加AI回复到其历史记录
        for (String userIdentifier : userIdsToProcess) {
            userChatHistoryService.addAIMessage(userIdentifier, aiResponse);
        }
        
        // 创建AI回复记录
        ChatRecord aiChatRecord = ChatRecord.builder()
                .userId(null)
                .nickname("小芸")
                .avatar("https://pic.blog.caiguoyu.cn/config/ai-avatar.png")  // 设置AI头像
                .content(aiResponse)
                .type(AI_MESSAGE.getType())
                .ipAddress("127.0.0.1")
                .ipSource("火山引擎")
                .build();
        
        // 保存AI回复记录
        chatRecordDao.insert(aiChatRecord);
        
        // 发送AI回复到所有客户端
        WebsocketMessageDTO aiMessageResponseDTO = WebsocketMessageDTO.builder()
                .type(AI_MESSAGE.getType())
                .data(aiChatRecord)
                .build();
        broadcastMessage(aiMessageResponseDTO);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() throws IOException {
        // 更新在线人数
        webSocketSet.remove(this);
        updateOnlineCount();
    }

    /**
     * 加载历史聊天记录
     *
     * @param endpointConfig 配置
     * @return 加载历史聊天记录
     */
    private ChatRecordDTO listChartRecords(EndpointConfig endpointConfig) {
        // 获取聊天历史记录
        List<ChatRecord> chatRecordList = chatRecordDao.selectList(new LambdaQueryWrapper<ChatRecord>()
                .ge(ChatRecord::getCreateTime, DateUtil.offsetHour(new Date(), -12)));
        // 获取当前用户ip
        String ipAddress = endpointConfig.getUserProperties().get(ChatConfigurator.HEADER_NAME).toString();

        return ChatRecordDTO.builder()
                .chatRecordList(chatRecordList)
                .ipAddress(ipAddress)
                .ipSource(IpUtils.getIpSource(ipAddress))
                .build();
    }

    /**
     * 更新在线人数
     *
     * @throws IOException io异常
     */
    @Async
    public void updateOnlineCount() throws IOException {
        // 获取当前在线人数
        WebsocketMessageDTO messageDTO = WebsocketMessageDTO.builder()
                .type(ONLINE_COUNT.getType())
                .data(webSocketSet.size())
                .build();
        // 广播消息
        broadcastMessage(messageDTO);
    }

    /**
     * 发送语音
     *
     * @param voiceVO 语音路径
     */
    public void sendVoice(VoiceVO voiceVO) {
        // 上传语音文件
        String content = uploadStrategyContext.executeUploadStrategy(voiceVO.getFile(), FilePathEnum.VOICE.getPath());
        voiceVO.setContent(content);
        // 保存记录
        ChatRecord chatRecord = BeanCopyUtils.copyObject(voiceVO, ChatRecord.class);
        // 替换用户昵称
        replaceNickname(chatRecord);
        chatRecordDao.insert(chatRecord);
        // 发送消息
        WebsocketMessageDTO messageDTO = WebsocketMessageDTO.builder()
                .type(VOICE_MESSAGE.getType())
                .data(chatRecord)
                .build();
        // 广播消息
        try {
            broadcastMessage(messageDTO);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 广播消息
     *
     * @param messageDTO 消息dto
     * @throws IOException io异常
     */
    private void broadcastMessage(WebsocketMessageDTO messageDTO) throws IOException {
        for (WebSocketServiceImpl webSocketService : webSocketSet) {
            synchronized (webSocketService.session) {
                webSocketService.session.getBasicRemote().sendText(JSON.toJSONString(messageDTO));
            }
        }
    }

    /**
     * 处理用户昵称
     *
     * @author caiguoyu
     * @date 2022/9/26
     */
    private void replaceNickname(ChatRecord chatRecord) {
        // 替换用户昵称
        String elderName = chatRecord.getNickname();
        if (StringUtils.isBlank(elderName) || Objects.equals("未知ip", elderName)
                || StringUtils.isBlank(chatRecord.getIpAddress())) {
            chatRecord.setNickname("未知领域的旅行者");
        } else {
//            String[] split = elderName.split("\\.");
//            if (split.length == 4) {
//                elderName = split[0] + "." + split[1] + "." + split[2] + "." + "***";
//            }
            elderName = chatRoomService.getRandomName(chatRecord.getIpAddress(), NicknameDictionary.NicknameTeamEnum.jinyongNickname);
            chatRecord.setNickname(IpUtils.getIpSource(chatRecord.getIpAddress()).split(" ")[0] + " " + elderName);
        }
    }
}
