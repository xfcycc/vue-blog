package com.minzheng.blog.controller;

import com.minzheng.blog.service.ChatRoomService;
import com.minzheng.blog.vo.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 聊天室控制器
 *
 * @author caiguoyu
 * @date 2022/9/26
 */
@RestController
@RequestMapping("/chatroom")
public class ChatRoomController {

    @Resource
    ChatRoomService chatRoomService;

    /**
     * 主动刷新聊天室匿名，也会生成第一次使用的匿名
     *
     * @param team 匿名组
     * @return java.lang.String
     * @author caiguoyu
     * @date 2022/9/26
     */
    @PostMapping("/refreshName")
    public Result<Object> refreshNickname(HttpServletRequest request, String team) {
        return chatRoomService.refreshNickname(request, team);
    }

    /**
     * 获取匿名名称
     *
     * @param request
     * @return com.minzheng.blog.vo.Result<java.lang.String>
     * @author caiguoyu
     * @date 2022/9/26
     */
    @GetMapping("/nickname")
    public Result<Object> getExistedName(HttpServletRequest request) {
        return chatRoomService.getExistedName(request);
    }

    /**
     * 获取匿名名称，不存在则创建
     *
     * @param request
     * @return com.minzheng.blog.vo.Result<java.lang.Object>
     * @author caiguoyu
     * @date 2022/9/26
     */
    @PostMapping("/nickname")
    public Result<Object> getName(HttpServletRequest request) {
        return chatRoomService.getName(request);
    }
}
