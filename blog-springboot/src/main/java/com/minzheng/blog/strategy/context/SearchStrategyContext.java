package com.minzheng.blog.strategy.context;

import com.minzheng.blog.dto.ArticleSearchDTO;
import com.minzheng.blog.strategy.SearchStrategy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 搜索策略上下文
 *
 * @author yezhiqiu
 * @date 2021/07/27
 */
@Service
public class SearchStrategyContext {
    @Resource(name = "mySqlSearchStrategyImpl")
    private SearchStrategy searchStrategy;

    /**
     * 执行搜索策略
     *
     * @param keywords 关键字
     * @return {@link List<ArticleSearchDTO>} 搜索文章
     */
    public List<ArticleSearchDTO> executeSearchStrategy(String keywords) {
        return searchStrategy.searchArticle(keywords);
    }

}
