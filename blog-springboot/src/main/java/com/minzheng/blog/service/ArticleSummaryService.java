package com.minzheng.blog.service;

/**
 * 文章概要服务
 *
 * @author caiguoyu
 */
public interface ArticleSummaryService {

    /**
     * 生成文章概要
     *
     * @param articleTitle   文章标题
     * @param articleContent 文章内容
     * @return 文章概要
     */
    String generateSummary(String articleTitle, String articleContent);

}
