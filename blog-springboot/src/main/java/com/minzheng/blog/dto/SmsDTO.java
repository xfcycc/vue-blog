package com.minzheng.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短信服务
 * @author caiguoyu
 * @date 2022/9/17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SmsDTO {
    /**
     * 手机号
     */
    private String phone;

    /**
     * 验证码
     */
    private String code;
}
