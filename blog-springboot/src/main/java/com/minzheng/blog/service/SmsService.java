package com.minzheng.blog.service;

import com.minzheng.blog.vo.Result;

/**
 * 短信服务
 *
 * @author caiguoyu
 * @date 2022/9/17
 */
public interface SmsService {


    /**
     * 简单的异步发送短信服务
     *
     * @param phone 手机号
     * @return com.minzheng.blog.vo.Result<?>
     * @author caiguoyu
     * @date 2022/9/17
     */
    Result<String> sendMessage(String phone);

    /**
     * 验证短信验证码
     *
     * @param phone
     * @param code
     * @return com.minzheng.blog.vo.Result<java.lang.String>
     * @author caiguoyu
     * @date 2022/9/17
     */
    Result<String> checkSmsCode(String phone, String code);
}
