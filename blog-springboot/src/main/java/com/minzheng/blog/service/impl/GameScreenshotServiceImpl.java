package com.minzheng.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minzheng.blog.dao.GameScreenshotDao;
import com.minzheng.blog.entity.GameScreenshot;
import com.minzheng.blog.service.GameScreenshotService;
import org.springframework.stereotype.Service;

/**
 * 游戏截图服务
 */
@Service
public class GameScreenshotServiceImpl extends ServiceImpl<GameScreenshotDao, GameScreenshot>
        implements GameScreenshotService {
}
