package com.minzheng.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 游客头像
 *
 * @author caiguoyu
 * @date 2026/05/18
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VisitorAvatarDTO {

    /**
     * 头像id
     */
    private Integer avatarId;

    /**
     * 头像地址
     */
    private String avatar;
}
