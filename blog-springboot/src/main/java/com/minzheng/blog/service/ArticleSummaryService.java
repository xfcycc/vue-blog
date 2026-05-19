package com.minzheng.blog.service;

/**
 * 文章概要服务
 *
 * @author caiguoyu
 */
public interface ArticleSummaryService {

    /**
     * 生成并保存文章概要
     *
     * @param articleId      文章id
     * @param articleTitle   文章标题
     * @param articleContent 文章内容
     */
    void generateAndSaveSummary(Integer articleId, String articleTitle, String articleContent);

}
