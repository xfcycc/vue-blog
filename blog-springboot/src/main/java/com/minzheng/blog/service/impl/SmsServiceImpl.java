package com.minzheng.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
import com.baomidou.mybatisplus.extension.api.R;
import com.google.gson.Gson;
import com.minzheng.blog.constant.RedisPrefixConst;
import com.minzheng.blog.dto.SmsDTO;
import com.minzheng.blog.service.RedisService;
import com.minzheng.blog.service.SmsService;
import com.minzheng.blog.util.CommonUtils;
import com.minzheng.blog.vo.Result;
import darabonba.core.client.ClientOverrideConfiguration;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.minzheng.blog.constant.MQPrefixConst.SMS_EXCHANGE;

/**
 * 短信服务
 *
 * @author caiguoyu
 * @date 2022/9/17
 */
@Service
@Log4j2
public class SmsServiceImpl implements SmsService {

    /**
     * 每日发送上限
     */
    private static final Integer SMS_LIMIT_DAILY = 5;

    @Resource
    private RedisService redisService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public Result<String> sendMessage(String phone) {
        String sendKey = RedisPrefixConst.SMS_SEND_SERVICE + "_" + phone;
        String limitKey = RedisPrefixConst.SMS_LIMIT_SERVICE + "_" + phone;
        String codeKey = RedisPrefixConst.SMS_CODE_SERVICE + "_" + phone;
        // 操作限制
        if (redisService.hasKey(sendKey)) {
            Long expireTime = redisService.getExpire(sendKey);
            return Result.fail("操作过于频繁，请稍后再试");
        }
        // 24小时发送限制
        if (redisService.hasKey(limitKey)) {
            Integer limit = (Integer) redisService.get(limitKey);
            if (limit >= SMS_LIMIT_DAILY) {
                return Result.fail("今日发送已达上限，请24小时后再试");
            } else {
                redisService.incr(limitKey, 1L);
            }
        }else {
            redisService.set(limitKey, 1L, 60L * 60L * 24L);
        }
        // 验证码及有效期

        String code = CommonUtils.getRandomCode().substring(0, 4);
        SmsDTO smsDTO = SmsDTO.builder().phone(phone).code(code).build();
         rabbitTemplate.convertAndSend(SMS_EXCHANGE, "*", new Message(JSON.toJSONBytes(smsDTO), new MessageProperties()));
        // 一分钟操作限制
        redisService.set(sendKey, phone, 60L);
        // 验证码有效期5分钟，以最后一次为准
        redisService.set(codeKey, code, 60L * 5L);
        return Result.ok();
    }

    @Override
    public Result<String> checkSmsCode(String phone, String code) {
        String codeKey = RedisPrefixConst.SMS_CODE_SERVICE + "_" + phone;
        if (!redisService.hasKey(codeKey)) {
            return Result.fail("验证码不存在或已过期！");
        }
        String trulyCode = (String) redisService.get(codeKey);
        if (!Objects.equals(trulyCode, code)) {
            return Result.fail("验证码不正确！");
        }
        redisService.del(codeKey);
        return Result.ok();
    }
}
