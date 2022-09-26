package com.minzheng.blog.service;

import com.minzheng.blog.entity.Article;

/**
 * 站点地图服务
 *
 * @author caiguoyu
 * @date 2022/9/25
 */
public interface SiteMapService {

    /**
     * 生成文章站点地图
     *
     * @return java.lang.String
     * @author caiguoyu
     * @date 2022/9/25
     */
    String createArticleMap();

    /**
     * 文章主动推送百度收录
     *
     * @param article
     * @author caiguoyu
     * @date 2022/9/26
     */
    void apiPull(Article article);
}
