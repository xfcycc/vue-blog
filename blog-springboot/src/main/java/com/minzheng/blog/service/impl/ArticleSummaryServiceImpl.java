package com.minzheng.blog.service.impl;

import com.minzheng.blog.service.ArticleSummaryService;
import com.minzheng.blog.tool.ArticleSummaryTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 文章概要服务
 *
 * @author caiguoyu
 */
@Slf4j
@Service
public class ArticleSummaryServiceImpl implements ArticleSummaryService {

    @Resource
    private ArticleSummaryTool articleSummaryTool;

    @Override
    public String generateSummary(String articleTitle, String articleContent) {
        try {
            return articleSummaryTool.generateSummary(articleTitle, articleContent);
        } catch (Exception e) {
            log.error("生成文章概要失败", e);
            return null;
        }
    }

}
