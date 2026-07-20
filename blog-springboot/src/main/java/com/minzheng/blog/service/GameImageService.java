package com.minzheng.blog.service;

import com.minzheng.blog.dto.GameImageSourceDTO;
import com.minzheng.blog.vo.GameImageSourceVO;

/**
 * 游戏图片服务
 */
public interface GameImageService {

    GameImageSourceDTO getImageSource(GameImageSourceVO sourceVO);
}
