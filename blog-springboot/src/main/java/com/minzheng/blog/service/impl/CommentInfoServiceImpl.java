package com.minzheng.blog.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minzheng.blog.entity.CommentInfo;
import com.minzheng.blog.dao.CommentInfoMapper;
import com.minzheng.blog.service.CommentInfoService;
/**
 * 
 * @author caiguoyu
 * @date 2022/9/16 
 */
@Service
public class CommentInfoServiceImpl extends ServiceImpl<CommentInfoMapper, CommentInfo> implements CommentInfoService{

}
