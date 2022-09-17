package com.minzheng.blog.controller;

import com.minzheng.blog.service.SmsService;
import com.minzheng.blog.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 短信服务控制器
 *
 * @author caiguoyu
 * @date 2022/9/16
 */
@Api(tags = "短信模块")
@RestController
@RequestMapping("/sms")
public class SmsController {

    @Resource
    SmsService smsService;

    @ApiOperation(value = "发送短信")
    @PostMapping("/send")
    public Result<String> sendSms(String phone) {
        return smsService.sendMessage(phone);
    }

    @ApiOperation(value = "验证短信验证码")
    @PostMapping("/check")
    public Result<String> checkSmsCode(String phone, String code) {
        return smsService.checkSmsCode(phone, code);
    }
}
