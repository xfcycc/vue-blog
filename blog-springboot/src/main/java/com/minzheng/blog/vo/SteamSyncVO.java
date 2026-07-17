package com.minzheng.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Steam同步参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SteamSyncVO {

    private Boolean includePlayedFreeGames;
}
