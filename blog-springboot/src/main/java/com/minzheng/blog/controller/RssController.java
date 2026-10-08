package com.minzheng.blog.controller;

import com.minzheng.blog.service.RssService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * RSS 订阅控制器。
 */
@RestController
public class RssController {

    @Resource
    private RssService rssService;

    /**
     * 获取公开文章订阅，无需请求参数。
     *
     * @return UTF-8 编码的 RSS 2.0 XML
     */
    @GetMapping(value = {"/rss", "/rss.xml"}, produces = "application/rss+xml;charset=UTF-8")
    public String getRss() {
        return rssService.createFeed();
    }
}
