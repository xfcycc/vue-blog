package com.minzheng.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minzheng.blog.dao.ArticleDao;
import com.minzheng.blog.dao.CategoryDao;
import com.minzheng.blog.entity.Article;
import com.minzheng.blog.entity.Category;
import com.minzheng.blog.service.BlogInfoService;
import com.minzheng.blog.service.RssService;
import com.minzheng.blog.vo.WebsiteConfigVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.minzheng.blog.enums.ArticleStatusEnum.PUBLIC;

/**
 * 公开文章 RSS 2.0 订阅。
 */
@Service
public class RssServiceImpl implements RssService {

    private static final String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";
    private static final String CONTENT_NAMESPACE = "http://purl.org/rss/1.0/modules/content/";
    private static final String DC_NAMESPACE = "http://purl.org/dc/elements/1.1/";
    private static final ZoneId SITE_ZONE = ZoneId.of("Asia/Shanghai");

    @Resource
    private ArticleDao articleDao;

    @Resource
    private CategoryDao categoryDao;

    @Resource
    private BlogInfoService blogInfoService;

    @Value("${website.url}")
    private String websiteUrl;

    /**
     * 查询最近 20 篇公开且未删除的文章，批量加载分类并生成订阅。
     *
     * @return RSS 2.0 XML
     */
    @Override
    public String createFeed() {
        List<Article> articles = articleDao.selectPage(new Page<Article>(1, 20, false),
                new LambdaQueryWrapper<Article>()
                        .select(Article::getId, Article::getCategoryId, Article::getArticleTitle,
                                Article::getArticleSummary, Article::getArticleContent, Article::getArticleCover,
                                Article::getCreateTime, Article::getUpdateTime)
                        .eq(Article::getIsDelete, 0)
                        .eq(Article::getStatus, PUBLIC.getStatus())
                        .orderByDesc(Article::getCreateTime, Article::getId)).getRecords();
        Set<Integer> categoryIds = articles.stream().map(Article::getCategoryId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Integer, String> categories = categoryIds.isEmpty() ? Collections.emptyMap() :
                categoryDao.selectBatchIds(categoryIds).stream()
                        .collect(Collectors.toMap(Category::getId, category -> text(category.getCategoryName())));
        WebsiteConfigVO config = blogInfoService.getWebsiteConfig();
        String baseUrl = websiteUrl.replaceAll("/+$", "");
        StringWriter output = new StringWriter();
        try {
            XMLStreamWriter xml = XMLOutputFactory.newFactory().createXMLStreamWriter(output);
            xml.writeStartDocument("UTF-8", "1.0");
            xml.writeStartElement("rss");
            xml.writeAttribute("version", "2.0");
            xml.writeNamespace("atom", ATOM_NAMESPACE);
            xml.writeNamespace("content", CONTENT_NAMESPACE);
            xml.writeNamespace("dc", DC_NAMESPACE);
            xml.writeStartElement("channel");
            xml.writeEmptyElement("atom", "link", ATOM_NAMESPACE);
            xml.writeAttribute("href", baseUrl + "/api/rss");
            xml.writeAttribute("rel", "self");
            xml.writeAttribute("type", "application/rss+xml");
            writeElement(xml, "title", config.getWebsiteName());
            writeElement(xml, "link", baseUrl);
            writeElement(xml, "description", config.getWebsiteIntro());
            writeElement(xml, "language", "zh-cn");
            LocalDateTime lastUpdate = articles.stream()
                    .map(article -> article.getUpdateTime() != null ? article.getUpdateTime() : article.getCreateTime())
                    .filter(Objects::nonNull).max(LocalDateTime::compareTo).orElse(null);
            if (lastUpdate != null) {
                writeElement(xml, "lastBuildDate", formatDate(lastUpdate));
            }
            for (Article article : articles) {
                String url = baseUrl + "/articles/" + article.getId();
                String summary = getSummary(article);
                xml.writeStartElement("item");
                writeElement(xml, "title", article.getArticleTitle());
                writeElement(xml, "link", url);
                writeElement(xml, "description", summary);
                xml.writeStartElement("dc", "creator", DC_NAMESPACE);
                xml.writeCharacters(text(config.getWebsiteAuthor()));
                xml.writeEndElement();
                if (categories.containsKey(article.getCategoryId())) {
                    writeElement(xml, "category", categories.get(article.getCategoryId()));
                }
                xml.writeStartElement("guid");
                xml.writeAttribute("isPermaLink", "true");
                xml.writeCharacters(url);
                xml.writeEndElement();
                if (article.getCreateTime() != null) {
                    writeElement(xml, "pubDate", formatDate(article.getCreateTime()));
                }
                String content = "<p>" + HtmlUtils.htmlEscape(summary, "UTF-8") + "</p>";
                if (StringUtils.hasText(article.getArticleCover())) {
                    content = "<p><img src=\"" + HtmlUtils.htmlEscape(article.getArticleCover(), "UTF-8")
                            + "\" alt=\"\"></p>" + content;
                }
                content += "<p><a href=\"" + HtmlUtils.htmlEscape(url, "UTF-8") + "\">阅读全文</a></p>";
                xml.writeStartElement("content", "encoded", CONTENT_NAMESPACE);
                xml.writeCharacters(text(content));
                xml.writeEndElement();
                xml.writeEndElement();
            }
            xml.writeEndElement();
            xml.writeEndElement();
            xml.writeEndDocument();
            xml.close();
            return output.toString();
        } catch (XMLStreamException e) {
            throw new IllegalStateException("生成 RSS 订阅失败", e);
        }
    }

    /**
     * 写入 XML 文本元素，由 XML writer 处理特殊字符转义。
     *
     * @param xml XML writer
     * @param name 元素名
     * @param value 文本内容，可为空
     * @throws XMLStreamException XML 写入失败
     */
    private void writeElement(XMLStreamWriter xml, String name, String value) throws XMLStreamException {
        xml.writeStartElement(name);
        xml.writeCharacters(text(value));
        xml.writeEndElement();
    }

    /**
     * 使用已有摘要，无摘要时从 Markdown 正文提取最多 300 个字符。
     *
     * @param article 公开文章
     * @return 纯文本摘要
     */
    private String getSummary(Article article) {
        String value = StringUtils.hasText(article.getArticleSummary())
                ? article.getArticleSummary() : article.getArticleContent();
        String summary = text(value).replaceAll("```[\\s\\S]*?```", " ")
                .replaceAll("!\\[[^]]*]\\([^)]+\\)", " ")
                .replaceAll("\\[([^]]+)]\\([^)]+\\)", "$1")
                .replaceAll("<[^>]+>", " ").replaceAll("[#>*_`]+", " ")
                .replaceAll("\\s+", " ").trim();
        int length = summary.codePointCount(0, summary.length());
        return length <= 300 ? summary : summary.substring(0, summary.offsetByCodePoints(0, 300)) + "…";
    }

    /**
     * 按站点时区生成 RSS 使用的 RFC 1123 日期。
     *
     * @param dateTime 数据库中的本地时间
     * @return 含时区的发布日期
     */
    private String formatDate(LocalDateTime dateTime) {
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(dateTime.atZone(SITE_ZONE));
    }

    /**
     * 兼容空文本并移除 XML 1.0 不允许的控制字符。
     *
     * @param value 原始文本，可为空
     * @return 可写入 XML 的文本
     */
    private String text(String value) {
        return value == null ? "" : value.replaceAll(
                "[^\\x{9}\\x{A}\\x{D}\\x{20}-\\x{D7FF}\\x{E000}-\\x{FFFD}\\x{10000}-\\x{10FFFF}]", "");
    }
}
