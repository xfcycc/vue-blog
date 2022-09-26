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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * @author caiguoyu
 * @date 2022/9/25
 */
@Service
public class SiteMapServiceImpl implements SiteMapService {
    @Resource
    private ArticleDao articleDao;

    @Value("${website.url}")
    private String baseUrl;

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
}
