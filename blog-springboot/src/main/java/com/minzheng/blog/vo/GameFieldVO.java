package com.minzheng.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 游戏自定义字段
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameFieldVO {

    private Integer id;

    private String fieldName;

    private String fieldValue;

    private String fieldUnit;

    private String fieldType;

    private String groupName;

    private Integer showOnCard;

    private Integer sortOrder;
}
