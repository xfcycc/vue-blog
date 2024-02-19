package com.minzheng.blog.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.minzheng.blog.constant.RedisPrefixConst;
import com.minzheng.blog.dto.UserDetailDTO;
import com.minzheng.blog.dto.VisitorDTO;
import com.minzheng.blog.entity.UserInfo;
import com.minzheng.blog.entity.UserRole;
import com.minzheng.blog.service.RedisService;
import com.minzheng.blog.service.UserInfoService;
import com.minzheng.blog.service.UserRoleService;
import com.minzheng.blog.vo.UserRoleVO;
import lombok.Synchronized;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Date;


/**
 * 用户工具类
 *
 * @author yezhiqiu
 * @date 2021/08/10
 */
@Component
public class UserUtils {

    /**
     * 获取当前登录用户
     *
     * @return 用户登录信息
     */
    public static UserDetailDTO getLoginUser() {
        try {
            return (UserDetailDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } catch (ClassCastException e) {
            // 匿名访问
            return null;
        }
    }

    /**
     * 获取游客信息
     */
    @Synchronized
    public static VisitorDTO getVisitorInfo() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        RedisService redisService = ApplicationContextUtils.getBean(RedisService.class);
        UserInfoService userInfoService = ApplicationContextUtils.getBean(UserInfoService.class);
        String ipAddress = IpUtils.getIpAddress(request);
        String ipString = ipAddress.replace("\\.", "");
        String ipSource = IpUtils.getIpSource(ipAddress);
        if (Boolean.TRUE.equals(redisService.hHasKey(RedisPrefixConst.ANONYMOUS_VISITOR, ipString))) {
            VisitorDTO visitorDTO = (VisitorDTO) redisService.hGet(RedisPrefixConst.ANONYMOUS_VISITOR, ipString);
            visitorDTO.setLastVisitTime(new Date());
            redisService.hSet(RedisPrefixConst.ANONYMOUS_VISITOR, ipString, visitorDTO);
            return visitorDTO;
        } else {
            // 创建游客
            UserInfo userInfo = new UserInfo();
            StringBuilder nickname = new StringBuilder();
            nickname.append("来自");
            String location;
            if (StringUtils.isNotBlank(ipSource)) {
                location = ipSource.split(" ")[0];
            } else {
                location = "无风之地";
            }
            if (location.contains("上海")) {
                location = "爱丁堡";
            }
            if (location.contains("南京")) {
                location = "六朝古都";
            }
            if (location.startsWith("北京")) {
                location = "天龙人地区";
            }
            nickname.append(location);
            nickname.append("的旅行者");
            userInfo.setNickname(nickname.toString());
            userInfo.setAvatar("https://pic.blog.caiguoyu.cn/config/miniq.png");
            userInfoService.save(userInfo);
            userInfoService.updateUserRole(UserRoleVO.builder().userInfoId(userInfo.getId()).roleIdList(Collections.singletonList(3)).build());
            VisitorDTO visitorDTO = VisitorDTO.builder().userId(userInfo.getId()).nickname(userInfo.getNickname()).avatar(userInfo.getAvatar())
                    .ipAddress(ipAddress).ipSource(ipSource).browser(IpUtils.getUserAgent(request).getBrowser().getName())
                    .lastVisitTime(new Date()).build();
            redisService.hSet(RedisPrefixConst.ANONYMOUS_VISITOR, ipString, visitorDTO);
            return visitorDTO;
        }
    }
}

