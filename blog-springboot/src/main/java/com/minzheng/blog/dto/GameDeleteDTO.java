package com.minzheng.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 游戏删除结果
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameDeleteDTO {

    private Integer deletedCount;

    private Integer skippedCount;
}
