package com.minzheng.blog.consumer;

import com.alibaba.fastjson.JSON;
import com.minzheng.blog.dto.EmailDTO;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;


import javax.annotation.Resource;

import static com.minzheng.blog.constant.MQPrefixConst.EMAIL_QUEUE;

/**
 * 通知邮箱
 *
 * @author yezhqiu
 * @date 2021/06/13
 * @since 1.0.0
 **/
@Component
@RabbitListener(queues = EMAIL_QUEUE)
public class EmailConsumer {

    /**
     * 邮箱号
     */
    @Value("${spring.mail.username}")
    private String email;

    /**
     * 个性化名称
     */
    @Value("${spring.mail.nickname}")
    private String nickname;

    @Resource
    private JavaMailSender javaMailSender;

    @RabbitHandler
    public void process(byte[] data) {
        EmailDTO emailDTO = JSON.parseObject(new String(data), EmailDTO.class);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(nickname + '<' + email + '>');
        message.setTo(emailDTO.getEmail());
        message.setSubject(emailDTO.getSubject());
        message.setText(emailDTO.getContent());
        javaMailSender.send(message);
    }

}
