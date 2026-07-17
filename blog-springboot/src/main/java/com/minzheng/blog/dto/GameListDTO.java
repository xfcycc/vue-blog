package com.minzheng.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 前台游戏列表结果
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameListDTO {

    private List<GameDTO> recordList;

    private Integer count;

    private Map<String, Integer> statusCountMap;

    private List<String> platformList;
}
