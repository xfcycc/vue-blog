package com.minzheng.blog.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文章状态枚举
 *
 * @author yezhiqiu
 * @date 2021/08/10
 */
@Getter
@AllArgsConstructor
public enum ArticleStatusEnum {
    /**
     * 公开
     */
    PUBLIC(1, "公开"),
    /**
     * 私密
     */
    SECRET(2, "私密"),
    /**
     * 草稿
     */
    DRAFT(3, "草稿"),

    /**
     * 以下为文章类型
     */
    DEFAULT_TYPE(0, "默认类型"),

    ORIGINAL(1, "原创"),

    REPRODUCED(2, "转载"),

    TRANSLATED(3, "翻译");

    /**
     * 状态
     */
    private final Integer status;

    /**
     * 描述
     */
    private final String desc;

}
