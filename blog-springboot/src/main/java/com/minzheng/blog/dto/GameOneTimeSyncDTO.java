package com.minzheng.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 游戏一次性同步结果
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameOneTimeSyncDTO {

    private Integer addedCount;

    private Integer updatedCount;

    private Integer skippedCount;
}
