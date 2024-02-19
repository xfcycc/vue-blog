package com.minzheng.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 游客信息
 * @author caiguoyu
 * @date 2024/2/18
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VisitorDTO {
    private Integer userId;

    private String nickname;

    private String avatar;

    private String ipAddress;

    private String ipSource;

    private String browser;

    private Date lastVisitTime;
}
