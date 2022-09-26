package com.minzheng.blog.service;

import com.minzheng.blog.vo.Result;

import javax.servlet.http.HttpServletRequest;

/**
 * @author caiguoyu
 * @date 2022/9/26
 */
public interface ChatRoomService {

    /**
     * 刷新匿名
     *
     * @param request
     * @param team
     * @return com.minzheng.blog.vo.Result<java.lang.String>
     * @author caiguoyu
     * @date 2022/9/26
     */
    Result<Object> refreshNickname(HttpServletRequest request, String team);

    /**
     * 获取已存在的匿名
     *
     * @param request
     * @return com.minzheng.blog.vo.Result<java.lang.String>
     * @author caiguoyu
     * @date 2022/9/26
     */
    Result<Object> getExistedName(HttpServletRequest request);

    /**
     * 生成名称（包括第一次）
     *
     * @param request
     * @return com.minzheng.blog.vo.Result<java.lang.Object>
     * @author caiguoyu
     * @date 2022/9/26
     */
    Result<Object> getName(HttpServletRequest request);
}
