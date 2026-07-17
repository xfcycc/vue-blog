package com.minzheng.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 删除游戏
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameDeleteVO {

    private List<Integer> idList;
}
