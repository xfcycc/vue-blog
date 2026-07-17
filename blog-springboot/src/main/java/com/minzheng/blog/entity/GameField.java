package com.minzheng.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 游戏自定义字段
 *
 * @author caiguoyu
 * @date 2026/07/17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_game_field")
public class GameField {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer gameId;

    private String fieldName;

    private String fieldValue;

    private String fieldUnit;

    private String fieldType;

    private String groupName;

    private Integer showOnCard;

    private Integer sortOrder;
}
