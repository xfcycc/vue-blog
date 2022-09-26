package com.minzheng.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;

/**
 * 匿名昵称
 *
 * @author caiguoyu
 * @date 2022/9/26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NicknameDTO implements Serializable {

    private static final long serialVersionUID = 1;

    /**
     * 名称
     */
    private String nickname;

    /**
     * 已刷新次数
     */
    private Integer count;
}
