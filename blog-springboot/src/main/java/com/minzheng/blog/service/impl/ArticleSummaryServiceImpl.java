package com.minzheng.blog.service.impl;

import com.minzheng.blog.dao.ArticleDao;
import com.minzheng.blog.service.ArticleSummaryService;
import com.minzheng.blog.tool.ArticleSummaryTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    @Resource
    private ArticleDao articleDao;

    @Override
    public void generateAndSaveSummary(Integer articleId, String articleTitle, String articleContent) {
        try {
            String summary = articleSummaryTool.generateSummary(articleTitle, articleContent);
            if (StringUtils.hasText(summary)) {
                articleDao.updateArticleSummary(articleId, summary);
            }
        } catch (Exception e) {
            log.error("保存文章概要失败，articleId={}", articleId, e);
        }
    }

}
