package com.minzheng.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minzheng.blog.constant.CommonConst;
import com.minzheng.blog.dao.ArticleDao;
import com.minzheng.blog.entity.Article;
import com.minzheng.blog.enums.ArticleStatusEnum;
import com.minzheng.blog.service.SiteMapService;
import com.redfin.sitemapgenerator.ChangeFreq;
import com.redfin.sitemapgenerator.WebSitemapGenerator;
import com.redfin.sitemapgenerator.WebSitemapUrl;
import com.squareup.okhttp.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * @author caiguoyu
 * @date 2022/9/25
 */
@Slf4j
@Service
public class SiteMapServiceImpl implements SiteMapService {
    @Resource
    private ArticleDao articleDao;

    @Value("${website.url}")
    private String baseUrl;

    @Value("${baidu.sitemap.site}")
    private String baiduSite;

    @Value("${baidu.sitemap.token}")
    private String baiduToken;

    @Override
    public String createArticleMap() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        WebSitemapGenerator wsg = null;
        try {
            // 站点地图目录
            wsg = new WebSitemapGenerator(baseUrl);
            // 首页数据
            WebSitemapUrl homeUrl = new WebSitemapUrl
                    // 地址
                    .Options(baseUrl)
                    // 更新时间
                    .lastMod(dtf.format(LocalDateTime.now()))
                    // 权重
                    .priority(1.0)
                    // 更新频率
                    .changeFreq(ChangeFreq.DAILY)
                    .build();
            wsg.addUrl(homeUrl);

            // 文章数据
            List<Article> articles = articleDao.selectList(new LambdaQueryWrapper<Article>()
                    .eq(Article::getIsDelete, CommonConst.FALSE)
                    .eq(Article::getStatus, ArticleStatusEnum.PUBLIC.getStatus())
                    .ne(Article::getType, ArticleStatusEnum.DEFAULT_TYPE.getStatus()));
            for (Article article : articles) {
                WebSitemapUrl articleUrl = new WebSitemapUrl
                        .Options(baseUrl + "/articles/" + article.getId())
                        .lastMod(dtf.format(article.getUpdateTime() != null ? article.getUpdateTime() : article.getCreateTime()))
                        .priority(0.9)
                        .changeFreq(ChangeFreq.DAILY)
                        .build();
                wsg.addUrl(articleUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.join("", Objects.requireNonNull(wsg).writeAsStrings());
    }

    /**
     * 文章主动推送百度收录
     *
     * @param article
     * @author caiguoyu
     * @date 2022/9/26
     */
    @Override
    @Async
    public void apiPull(Article article) {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("text/plain");
        String content = baseUrl + "/articles/" + article.getId();
        RequestBody body = RequestBody.create(mediaType, content);
        String url = "http://data.zz.baidu.com/urls?site=" + baiduSite + "&token=" + baiduToken;
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("User-Agent", "curl/7.12.1")
                .addHeader("Content-Type", "text/plain")
                .addHeader("Accept", "*/*")
                .addHeader("Host", "data.zz.baidu.com")
                .addHeader("Content-Length", "83")
                .build();
        try {
            Response response = client.newCall(request).execute();
            log.info("文章推送结果：{}", response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
