package com.minzheng.blog.controller;

import com.minzheng.blog.dao.ArticleDao;
import com.minzheng.blog.dto.ArticleDTO;
import com.minzheng.blog.dto.TagDTO;
import com.minzheng.blog.exception.BizException;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * SEO 静态内容控制器
 *
 * @author caiguoyu
 * @date 2026/05/21
 */
@RestController
public class SeoController {

    private static final String SITE_NAME = "菜鸟的小站";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Pattern IMAGE_PATTERN = Pattern.compile("^!\\[([^]]*)]\\(([^)]+)\\)\\s*$");

    @Resource
    private ArticleDao articleDao;

    /**
     * 文章 SEO HTML
     *
     * @param articleId 文章id
     * @return HTML
     */
    @GetMapping(value = "/seo/articles/{articleId}", produces = MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
    public String getArticleSeoHtml(@PathVariable("articleId") Integer articleId, HttpServletRequest request) {
        ArticleDTO article = articleDao.getArticleById(articleId);
        if (article == null) {
            throw new BizException("文章不存在");
        }
        return buildArticleHtml(article, request);
    }

    private String buildArticleHtml(ArticleDTO article, HttpServletRequest request) {
        String canonicalUrl = getRequestBaseUrl(request) + "/articles/" + article.getId();
        String title = safeText(article.getArticleTitle());
        String pageTitle = title + " - " + SITE_NAME;
        String description = buildDescription(article);
        String keywords = buildKeywords(article.getTagDTOList());
        String cover = safeText(article.getArticleCover());
        String articleHtml = renderMarkdownForSeo(article.getArticleContent());
        String publishTime = formatDateTime(article.getCreateTime());
        String updateTime = formatDateTime(article.getUpdateTime() != null ? article.getUpdateTime() : article.getCreateTime());

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"zh-CN\">\n");
        html.append("<head>\n");
        html.append("  <meta charset=\"UTF-8\">\n");
        html.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("  <title>").append(escapeHtml(pageTitle)).append("</title>\n");
        html.append("  <meta name=\"description\" content=\"").append(escapeHtml(description)).append("\">\n");
        if (StringUtils.hasText(keywords)) {
            html.append("  <meta name=\"keywords\" content=\"").append(escapeHtml(keywords)).append("\">\n");
        }
        html.append("  <link rel=\"canonical\" href=\"").append(escapeHtml(canonicalUrl)).append("\">\n");
        html.append("  <meta property=\"og:type\" content=\"article\">\n");
        html.append("  <meta property=\"og:title\" content=\"").append(escapeHtml(pageTitle)).append("\">\n");
        html.append("  <meta property=\"og:description\" content=\"").append(escapeHtml(description)).append("\">\n");
        html.append("  <meta property=\"og:url\" content=\"").append(escapeHtml(canonicalUrl)).append("\">\n");
        if (StringUtils.hasText(cover)) {
            html.append("  <meta property=\"og:image\" content=\"").append(escapeHtml(cover)).append("\">\n");
        }
        html.append("  <script type=\"application/ld+json\">").append(buildStructuredData(article, canonicalUrl, description)).append("</script>\n");
        html.append("  <style>");
        html.append("body{margin:0;background:#f8fafc;color:#1f2937;font-family:-apple-system,BlinkMacSystemFont,\"Segoe UI\",Arial,\"PingFang SC\",\"Microsoft YaHei\",sans-serif;line-height:1.8;}");
        html.append(".seo-page{max-width:860px;margin:0 auto;padding:40px 20px 64px;}");
        html.append(".seo-article{background:#fff;border:1px solid #e5e7eb;border-radius:12px;padding:32px;box-shadow:0 12px 40px rgba(15,23,42,.06);}");
        html.append("h1{font-size:32px;line-height:1.3;margin:0 0 14px;color:#0f172a;}");
        html.append(".meta{color:#64748b;font-size:14px;margin-bottom:24px;}");
        html.append(".cover{width:100%;max-height:420px;object-fit:cover;border-radius:10px;margin:8px 0 24px;}");
        html.append(".summary{background:#f1f5f9;border-left:4px solid #0ea5e9;padding:14px 16px;margin:0 0 24px;color:#334155;}");
        html.append(".content h2,.content h3,.content h4,.content h5,.content h6{margin:28px 0 10px;color:#111827;line-height:1.4;}");
        html.append(".content p{margin:12px 0;}.content pre{overflow:auto;background:#0f172a;color:#e5e7eb;border-radius:8px;padding:16px;}");
        html.append(".content img{max-width:100%;border-radius:8px;}.tags{margin-top:28px;color:#64748b;font-size:14px;}");
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("  <main class=\"seo-page\">\n");
        html.append("    <article class=\"seo-article\">\n");
        html.append("      <h1>").append(escapeHtml(title)).append("</h1>\n");
        html.append("      <div class=\"meta\">发布于 ").append(escapeHtml(publishTime)).append("，更新于 ").append(escapeHtml(updateTime));
        if (StringUtils.hasText(article.getCategoryName())) {
            html.append("，分类：").append(escapeHtml(article.getCategoryName()));
        }
        html.append("</div>\n");
        if (StringUtils.hasText(cover)) {
            html.append("      <img class=\"cover\" src=\"").append(escapeHtml(cover)).append("\" alt=\"").append(escapeHtml(title)).append("\">\n");
        }
        if (StringUtils.hasText(description)) {
            html.append("      <section class=\"summary\">").append(escapeHtml(description)).append("</section>\n");
        }
        html.append("      <section class=\"content\">").append(articleHtml).append("</section>\n");
        if (StringUtils.hasText(keywords)) {
            html.append("      <div class=\"tags\">标签：").append(escapeHtml(keywords)).append("</div>\n");
        }
        html.append("    </article>\n");
        html.append("  </main>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        return html.toString();
    }

    private String buildDescription(ArticleDTO article) {
        if (StringUtils.hasText(article.getArticleSummary())) {
            return limitText(article.getArticleSummary(), 160);
        }
        return limitText(stripMarkdown(article.getArticleContent()), 160);
    }

    private String buildKeywords(List<TagDTO> tagList) {
        if (tagList == null || tagList.isEmpty()) {
            return "";
        }
        return tagList.stream()
                .map(TagDTO::getTagName)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.joining(","));
    }

    private String renderMarkdownForSeo(String markdown) {
        if (!StringUtils.hasText(markdown)) {
            return "";
        }
        StringBuilder html = new StringBuilder();
        StringBuilder paragraph = new StringBuilder();
        StringBuilder code = new StringBuilder();
        boolean inCode = false;
        String[] lines = markdown.replace("\r\n", "\n").replace('\r', '\n').split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("```")) {
                if (inCode) {
                    html.append("<pre><code>").append(escapeHtml(code.toString())).append("</code></pre>\n");
                    code.setLength(0);
                    inCode = false;
                } else {
                    flushParagraph(html, paragraph);
                    inCode = true;
                }
                continue;
            }
            if (inCode) {
                code.append(line).append('\n');
                continue;
            }
            if (!StringUtils.hasText(trimmed)) {
                flushParagraph(html, paragraph);
                continue;
            }
            Matcher imageMatcher = IMAGE_PATTERN.matcher(trimmed);
            if (imageMatcher.matches()) {
                flushParagraph(html, paragraph);
                html.append("<p><img src=\"").append(escapeHtml(imageMatcher.group(2)))
                        .append("\" alt=\"").append(escapeHtml(imageMatcher.group(1))).append("\"></p>\n");
                continue;
            }
            int headingLevel = getHeadingLevel(trimmed);
            if (headingLevel > 0) {
                flushParagraph(html, paragraph);
                String heading = trimmed.substring(headingLevel + 1).trim();
                html.append("<h").append(headingLevel).append(">")
                        .append(escapeHtml(heading))
                        .append("</h").append(headingLevel).append(">\n");
                continue;
            }
            if (paragraph.length() > 0) {
                paragraph.append(' ');
            }
            paragraph.append(trimmed);
        }
        if (inCode) {
            html.append("<pre><code>").append(escapeHtml(code.toString())).append("</code></pre>\n");
        }
        flushParagraph(html, paragraph);
        return html.toString();
    }

    private void flushParagraph(StringBuilder html, StringBuilder paragraph) {
        if (paragraph.length() == 0) {
            return;
        }
        html.append("<p>").append(escapeHtml(paragraph.toString())).append("</p>\n");
        paragraph.setLength(0);
    }

    private int getHeadingLevel(String line) {
        int level = 0;
        while (level < line.length() && line.charAt(level) == '#') {
            level++;
        }
        if (level > 0 && level <= 6 && line.length() > level && line.charAt(level) == ' ') {
            return level;
        }
        return 0;
    }

    private String buildStructuredData(ArticleDTO article, String canonicalUrl, String description) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        appendJsonField(json, "@context", "https://schema.org", false);
        appendJsonField(json, "@type", "BlogPosting", true);
        appendJsonField(json, "headline", article.getArticleTitle(), true);
        appendJsonField(json, "description", description, true);
        appendJsonField(json, "url", canonicalUrl, true);
        appendJsonField(json, "datePublished", formatDateTime(article.getCreateTime()), true);
        appendJsonField(json, "dateModified", formatDateTime(article.getUpdateTime() != null ? article.getUpdateTime() : article.getCreateTime()), true);
        if (StringUtils.hasText(article.getArticleCover())) {
            appendJsonField(json, "image", article.getArticleCover(), true);
        }
        json.append(",\"author\":{\"@type\":\"Person\",\"name\":\"").append(escapeJson("蔡国钰")).append("\"}");
        json.append("}");
        return json.toString();
    }

    private void appendJsonField(StringBuilder json, String name, String value, boolean prependComma) {
        if (prependComma) {
            json.append(',');
        }
        json.append('"').append(escapeJson(name)).append("\":\"").append(escapeJson(safeText(value))).append('"');
    }

    private String stripMarkdown(String markdown) {
        if (!StringUtils.hasText(markdown)) {
            return "";
        }
        return markdown.replaceAll("```[\\s\\S]*?```", " ")
                .replaceAll("!\\[[^]]*]\\([^)]+\\)", " ")
                .replaceAll("\\[([^]]+)]\\([^)]+\\)", "$1")
                .replaceAll("[#>*_`\\-]", " ")
                .replaceAll("<[^>]+>", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String limitText(String text, int maxLength) {
        String value = stripMarkdown(text);
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return DATE_TIME_FORMATTER.format(dateTime);
    }

    private String getRequestBaseUrl(HttpServletRequest request) {
        String host = safeText(request.getHeader("X-Forwarded-Host"));
        if (!StringUtils.hasText(host)) {
            host = safeText(request.getHeader("Host"));
        }
        if (!StringUtils.hasText(host)) {
            host = request.getServerName();
        }
        String scheme = safeText(request.getHeader("X-Forwarded-Proto"));
        if (!StringUtils.hasText(scheme)) {
            scheme = host.endsWith("caiguoyu.cn") ? "https" : request.getScheme();
        }
        return scheme + "://" + host;
    }

    private String safeText(String text) {
        return text == null ? "" : text.trim();
    }

    private String escapeHtml(String text) {
        return HtmlUtils.htmlEscape(safeText(text), "UTF-8");
    }

    private String escapeJson(String text) {
        return safeText(text)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
