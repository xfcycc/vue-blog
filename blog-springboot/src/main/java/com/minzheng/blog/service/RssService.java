package com.minzheng.blog.service;

/**
 * RSS 订阅服务。
 */
public interface RssService {

    /**
     * 生成最近的公开文章订阅，无需输入参数。
     *
     * @return RSS 2.0 XML
     */
    String createFeed();
}
