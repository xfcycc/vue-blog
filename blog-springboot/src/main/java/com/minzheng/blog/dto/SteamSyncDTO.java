package com.minzheng.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Steam同步结果
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SteamSyncDTO {

    private Integer addedCount;

    private Integer updatedCount;

    private Integer skippedCount;
}
