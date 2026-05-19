package com.minzheng.blog.tool;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.minzheng.blog.config.DeepSeekConfigProperties;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 文章概要生成工具
 *
 * @author caiguoyu
 */
@Slf4j
@Component
public class ArticleSummaryTool {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json;charset=utf-8");

    private static final int SUMMARY_LENGTH = 200;

    @Resource
    private DeepSeekConfigProperties deepSeekConfigProperties;

    /**
     * 生成50字概要
     *
     * @param articleTitle   文章标题
     * @param articleContent 文章内容
     * @return 文章概要
     */
    public String generateSummary(String articleTitle, String articleContent) {
        if (!isAvailable() || !StringUtils.hasText(articleContent)) {
            return null;
        }
        JSONObject requestJson = buildRequestJson(articleTitle, articleContent);
        Request request = new Request.Builder()
                .url(getChatCompletionsUrl())
                .post(RequestBody.create(JSON_MEDIA_TYPE, requestJson.toJSONString()))
                .addHeader("Authorization", "Bearer " + deepSeekConfigProperties.getApiKey())
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            if (!response.isSuccessful()) {
                log.error("DeepSeek生成文章概要失败：{}", responseBody);
                return null;
            }
            return limitSummary(parseSummary(responseBody));
        } catch (IOException e) {
            log.error("DeepSeek生成文章概要异常", e);
            return null;
        }
    }

    private boolean isAvailable() {
        return Boolean.TRUE.equals(deepSeekConfigProperties.getEnabled())
                && StringUtils.hasText(deepSeekConfigProperties.getApiKey());
    }

    private JSONObject buildRequestJson(String articleTitle, String articleContent) {
        JSONObject requestJson = new JSONObject();
        requestJson.put("model", deepSeekConfigProperties.getModel());
        requestJson.put("temperature", 0.2);
        requestJson.put("max_tokens", deepSeekConfigProperties.getMaxTokens());
        requestJson.put("thinking", buildThinkingConfig());
        requestJson.put("stream", false);

        JSONArray messages = new JSONArray();
        messages.add(buildMessage("system", deepSeekConfigProperties.getSummaryPrompt()));
        messages.add(buildMessage("user", "标题：" + articleTitle + "\n正文：\n" + articleContent));
        requestJson.put("messages", messages);
        return requestJson;
    }

    private JSONObject buildMessage(String role, String content) {
        JSONObject message = new JSONObject();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private JSONObject buildThinkingConfig() {
        JSONObject thinking = new JSONObject();
        thinking.put("type", Boolean.TRUE.equals(deepSeekConfigProperties.getThinkingEnabled()) ? "enabled" : "disabled");
        return thinking;
    }

    private String getChatCompletionsUrl() {
        String baseUrl = deepSeekConfigProperties.getBaseUrl();
        if (!StringUtils.hasText(baseUrl)) {
            baseUrl = "https://api.deepseek.com";
        }
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        if (baseUrl.endsWith("/chat/completions")) {
            return baseUrl;
        }
        return baseUrl + "/chat/completions";
    }

    private String parseSummary(String responseBody) {
        JSONObject responseJson = JSONObject.parseObject(responseBody);
        JSONArray choices = responseJson.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        JSONObject message = choices.getJSONObject(0).getJSONObject("message");
        if (message == null) {
            return null;
        }
        String content = message.getString("content");
        if (!StringUtils.hasText(content)) {
            log.error("DeepSeek未返回最终概要：{}", responseBody);
        }
        return content;
    }

    private String limitSummary(String summary) {
        if (!StringUtils.hasText(summary)) {
            return null;
        }
        summary = summary.trim()
                .replaceAll("[\\r\\n]+", "")
                .replaceAll("^([\"'“”‘’]+)|([\"'“”‘’]+)$", "");
        if (summary.length() > SUMMARY_LENGTH) {
            return summary.substring(0, SUMMARY_LENGTH);
        }
        return summary;
    }

}
