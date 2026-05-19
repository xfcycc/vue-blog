package com.minzheng.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minzheng.blog.dao.UserInfoDao;
import com.minzheng.blog.dto.EmailDTO;
import com.minzheng.blog.dto.MessageBackDTO;
import com.minzheng.blog.service.BlogInfoService;
import com.minzheng.blog.service.MessageSseService;
import com.minzheng.blog.util.AvatarUtils;
import com.minzheng.blog.util.HTMLUtils;
import com.minzheng.blog.util.PageUtils;
import com.minzheng.blog.vo.ConditionVO;
import com.minzheng.blog.vo.PageResult;
import com.minzheng.blog.vo.MessageVO;
import com.minzheng.blog.dto.MessageDTO;
import com.minzheng.blog.entity.Message;
import com.minzheng.blog.dao.MessageDao;
import com.minzheng.blog.service.MessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minzheng.blog.util.BeanCopyUtils;
import com.minzheng.blog.util.IpUtils;
import com.minzheng.blog.vo.ReviewVO;
import com.minzheng.blog.vo.WebsiteConfigVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.minzheng.blog.constant.CommonConst.BLOGGER_ID;
import static com.minzheng.blog.constant.CommonConst.FALSE;
import static com.minzheng.blog.constant.CommonConst.TRUE;

/**
 * 留言服务
 *
 * @author yezhiqiu
 * @date 2021/07/28
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageDao, Message> implements MessageService {
    @Resource
    private MessageDao messageDao;
    @Resource
    private HttpServletRequest request;
    @Resource
    private BlogInfoService blogInfoService;
    @Resource
    private MessageSseService messageSseService;
    @Resource
    private UserInfoDao userInfoDao;
    @Resource
    private JavaMailSender javaMailSender;
    @Value("${website.url}")
    private String websiteUrl;
    @Value("${spring.mail.username}")
    private String mailUsername;
    @Value("${spring.mail.nickname}")
    private String mailNickname;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveMessage(MessageVO messageVO) {
        // 判断是否需要审核
        WebsiteConfigVO websiteConfig = blogInfoService.getWebsiteConfig();
        Integer isReview = websiteConfig.getIsMessageReview();
        // 获取用户ip
        String ipAddress = IpUtils.getIpAddress(request);
        String ipSource = IpUtils.getIpSource(ipAddress);
        Message message = BeanCopyUtils.copyObject(messageVO, Message.class);
        message.setMessageContent(HTMLUtils.filter(message.getMessageContent()));
        message.setIpAddress(ipAddress);
        message.setIsReview(isReview == TRUE ? FALSE : TRUE);
        message.setIpSource(ipSource);
        message.setAvatar(AvatarUtils.getVisitorAvatar(request).getAvatar());
        messageDao.insert(message);
        if (message.getIsReview().equals(TRUE)) {
            MessageDTO messageDTO = BeanCopyUtils.copyObject(message, MessageDTO.class);
            messageDTO.setClientId(messageVO.getClientId());
            messageSseService.sendMessage(messageDTO);
        }
        // 判断是否开启邮箱通知,通知博主
        if (websiteConfig.getIsEmailNotice().equals(TRUE)) {
            CompletableFuture.runAsync(() -> notice(message));
        }
    }

    @Override
    public List<MessageDTO> listMessages() {
        // 查询留言列表
        List<Message> messageList = messageDao.selectList(new LambdaQueryWrapper<Message>()
                .select(Message::getId, Message::getNickname, Message::getAvatar, Message::getMessageContent, Message::getTime)
                .eq(Message::getIsReview, TRUE));
        return BeanCopyUtils.copyList(messageList, MessageDTO.class);
    }

    @Override
    public SseEmitter subscribeMessages() {
        return messageSseService.subscribe();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateMessagesReview(ReviewVO reviewVO) {
        // 修改留言审核状态
        List<Message> messageList = reviewVO.getIdList().stream().map(item -> Message.builder()
                        .id(item)
                        .isReview(reviewVO.getIsReview())
                        .build())
                .collect(Collectors.toList());
        this.updateBatchById(messageList);
    }

    @Override
    public PageResult<MessageBackDTO> listMessageBackDTO(ConditionVO condition) {
        // 分页查询留言列表
        Page<Message> page = new Page<>(PageUtils.getCurrent(), PageUtils.getSize());
        LambdaQueryWrapper<Message> messageLambdaQueryWrapper = new LambdaQueryWrapper<Message>()
                .like(StringUtils.isNotBlank(condition.getKeywords()), Message::getNickname, condition.getKeywords())
                .eq(Objects.nonNull(condition.getIsReview()), Message::getIsReview, condition.getIsReview())
                .orderByDesc(Message::getId);
        Page<Message> messagePage = messageDao.selectPage(page, messageLambdaQueryWrapper);
        // 转换DTO
        List<MessageBackDTO> messageBackDTOList = BeanCopyUtils.copyList(messagePage.getRecords(), MessageBackDTO.class);
        return new PageResult<>(messageBackDTOList, (int) messagePage.getTotal());
    }

    /**
     * 通知博主有新留言
     *
     * @param message 留言信息
     */
    private void notice(Message message) {
        String adminEmail = userInfoDao.selectById(BLOGGER_ID).getEmail();
        if (StringUtils.isBlank(adminEmail)) {
            return;
        }
        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setEmail(adminEmail);
        if (message.getIsReview().equals(TRUE)) {
            emailDTO.setSubject("留言提醒");
            emailDTO.setContent("您收到了一条新的留言，请前往" + websiteUrl + "/message\n页面查看\n\n"
                    + "昵称：" + message.getNickname() + "\n"
                    + "内容：" + message.getMessageContent());
        } else {
            emailDTO.setSubject("审核提醒");
            emailDTO.setContent("您收到了一条新的留言，请前往后台管理页面审核\n\n"
                    + "昵称：" + message.getNickname() + "\n"
                    + "内容：" + message.getMessageContent());
        }
        sendEmail(emailDTO);
    }

    private void sendEmail(EmailDTO emailDTO) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailNickname + '<' + mailUsername + '>');
        message.setTo(emailDTO.getEmail());
        message.setSubject(emailDTO.getSubject());
        message.setText(emailDTO.getContent());
        javaMailSender.send(message);
    }

}
