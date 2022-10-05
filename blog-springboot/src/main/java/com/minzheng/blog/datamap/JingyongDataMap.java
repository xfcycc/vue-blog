package com.minzheng.blog.datamap;

import java.util.Set;

/**
 * 金庸花名字典
 *
 * @author caiguoyu
 * @date 2022/10/5
 */
public interface JingyongDataMap {

    /**
     * 返回set集合
     *
     * @return java.util.Set<java.lang.String>
     * @author caiguoyu
     * @date 2022/10/5
     */
    Set<String> defaultSet();

    /**
     * 返回数组
     *
     * @return java.lang.String[]
     * @author caiguoyu
     * @date 2022/10/5
     */
    String[] defaultArray();
}
