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
 * 游戏平台同步配置
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_game_config")
public class GameConfig {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String platform;

    private String configName;

    private String configKey;

    private String configValue;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
