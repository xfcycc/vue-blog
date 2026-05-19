package com.minzheng.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * DeepSeek配置属性
 *
 * @author caiguoyu
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekConfigProperties {

    /**
     * 是否启用概要生成
     */
    private Boolean enabled = true;

    /**
     * OpenAI兼容接口地址
     */
    private String baseUrl = "https://api.deepseek.com";

    /**
     * API Key
     */
    private String apiKey;

    /**
     * 模型
     */
    private String model = "deepseek-v4-flash";

    /**
     * 是否启用思考模式
     */
    private Boolean thinkingEnabled = false;

    /**
     * 最大输出token数
     */
    private Integer maxTokens = 512;

    /**
     * 生成文章概要的提示词
     */
    private String summaryPrompt = "你是个人技术博客首页文章卡片的摘要生成器。摘要展示在标题下方，用一句自然中文告诉读者这篇内容讲什么。请先判断文章类型：如果是事故复盘、踩坑记录或个人经历，优先描述发生了什么事件、如何处理以及结果或经验；如果是教程或技术笔记，概括解决的问题、核心技术和读者收获。摘要尽量写到45到50个中文字符，不能少于35个中文字符。不要机械重复标题，但可以保留关键事件主体；不要堆关键词，不要写成目录；不要出现“本文”“文章”“作者”“概要”等字样；不要使用Markdown、引号、换行、序号或夸张营销语；只输出摘要本身。";

}
