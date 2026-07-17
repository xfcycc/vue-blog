package com.minzheng.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minzheng.blog.dao.GameFieldDao;
import com.minzheng.blog.entity.GameField;
import com.minzheng.blog.service.GameFieldService;
import org.springframework.stereotype.Service;

/**
 * 游戏自定义字段服务
 */
@Service
public class GameFieldServiceImpl extends ServiceImpl<GameFieldDao, GameField> implements GameFieldService {
}
