package com.minzheng.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 游戏截图
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_game_screenshot")
public class GameScreenshot {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer gameId;

    private String originalUrl;

    private String displayUrl;

    private Integer originalWidth;

    private Integer originalHeight;

    private Integer displayWidth;

    private Integer displayHeight;

    private String frameType;

    private Integer columnSpan;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
