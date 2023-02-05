package com.minzheng.blog.consumer;

import com.alibaba.fastjson.JSON;
import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
import com.google.gson.Gson;
import com.minzheng.blog.dto.SmsDTO;
import com.minzheng.blog.service.RedisService;
import com.minzheng.blog.vo.Result;
import darabonba.core.client.ClientOverrideConfiguration;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.minzheng.blog.constant.MQPrefixConst.SMS_QUEUE;

/**
 * 短信消费者
 *
 * @author caiguoyu
 * @date 2022/9/17
 */
@Component
@RabbitListener(queues = SMS_QUEUE)
@Log4j2
public class SmsConsumer {
    @Value("${upload.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${upload.oss.accessKeySecret}")
    private String accessKeySecret;

    @Resource
    private RedisService redisService;

    @RabbitHandler
    public void process(byte[] data) {
        SmsDTO smsDTO = JSON.parseObject(new String(data), SmsDTO.class);
        StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
                .accessKeyId(accessKeyId)
                .accessKeySecret(accessKeySecret)
                //.securityToken("<your-token>") // use STS token
                .build());

        // Configure the Client
        AsyncClient client = AsyncClient.builder()
                .region("cn-hangzhou") // Region ID
                //.httpClient(httpClient) // Use the configured HttpClient, otherwise use the default HttpClient (Apache HttpClient)
                .credentialsProvider(provider)
                //.serviceConfiguration(Configuration.create()) // Service-level configuration
                // Client-level configuration rewrite, can set Endpoint, Http request parameters, etc.
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                                .setEndpointOverride("dysmsapi.aliyuncs.com")
                        //.setReadTimeout(Duration.ofSeconds(30))
                )
                .build();

        // Parameter settings for API request
        SendSmsRequest sendSmsRequest = SendSmsRequest.builder()
                .signName("菜鸟的小站")
                .templateCode("SMS_269230410")
                .phoneNumbers(smsDTO.getPhone())
                .templateParam("{\"code\":" + smsDTO.getCode() + "}")
                // Request-level configuration rewrite, can set Http request parameters, etc.
                // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
                .build();

        // Asynchronously get the return value of the API request
        CompletableFuture<SendSmsResponse> response = client.sendSms(sendSmsRequest);
        // Synchronously get the return value of the API request
        try {
            SendSmsResponse resp = response.get();
            System.out.println(new Gson().toJson(resp));
            // Asynchronous processing of return values
        /*response.thenAccept(resp -> {
            System.out.println(new Gson().toJson(resp));
        }).exceptionally(throwable -> { // Handling exceptions
            System.out.println(throwable.getMessage());
            return null;
        });*/

            // Finally, close the client
            client.close();
        } catch (InterruptedException | ExecutionException e) {
            log.warn("短信服务调用失败");
        }finally {
            client.close();
        }
    }
}
