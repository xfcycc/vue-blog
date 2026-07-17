package com.minzheng.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PSN一次性同步参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PsnOneTimeSyncVO {

    private String npsso;
}
