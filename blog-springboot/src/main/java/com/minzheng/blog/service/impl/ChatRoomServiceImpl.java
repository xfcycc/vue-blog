package com.minzheng.blog.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.minzheng.blog.constant.NicknameConst;
import com.minzheng.blog.constant.RedisPrefixConst;
import com.minzheng.blog.dto.NicknameDTO;
import com.minzheng.blog.service.ChatRoomService;
import com.minzheng.blog.service.RedisService;
import com.minzheng.blog.util.IpUtils;
import com.minzheng.blog.vo.Result;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author caiguoyu
 * @date 2022/9/26
 */
@Service
public class ChatRoomServiceImpl implements ChatRoomService {

    @Resource
    RedisService redisService;

    /**
     * 刷新匿名
     *
     * @param request
     * @param team
     * @return com.minzheng.blog.vo.Result<java.lang.String>
     * @author caiguoyu
     * @date 2022/9/26
     */
    @Override
    public Result<Object> refreshNickname(HttpServletRequest request, String team) {
        String ipAddress = IpUtils.getIpAddress(request);
        if (StringUtils.isBlank(ipAddress)) {
            return Result.fail(404, "ip地址未知");
        }
        if (NicknameConst.findEnum(team) == null) {
            return Result.fail(404, "分类未知");
        }
        String name = IpUtils.refreshRandomName(ipAddress, NicknameConst.findEnum(team));
        if (StringUtils.isBlank(name)) {
            return Result.fail(203, "刷新次数已达上限，请12小时后再试！");
        }
        String key = RedisPrefixConst.IP_ADDRESS_NICKNAME + "_" + ipAddress;
        NicknameDTO nickNameDTO = (NicknameDTO) redisService.get(key);
        return Result.ok(nickNameDTO);
    }

    /**
     * 获取已存在的匿名
     *
     * @param request
     * @return com.minzheng.blog.vo.Result<java.lang.String>
     * @author caiguoyu
     * @date 2022/9/26
     */
    @Override
    public Result<Object> getExistedName(HttpServletRequest request) {
        String ipAddress = IpUtils.getIpAddress(request);
        String key = RedisPrefixConst.IP_ADDRESS_NICKNAME + "_" + ipAddress;
        if (redisService.hasKey(key)) {
            NicknameDTO nickNameDTO = (NicknameDTO) redisService.get(key);
            return Result.ok(nickNameDTO);
        }
        return Result.fail();
    }

    /**
     * 生成名称（包括第一次）
     *
     * @param request
     * @return com.minzheng.blog.vo.Result<java.lang.Object>
     * @author caiguoyu
     * @date 2022/9/26
     */
    @Override
    public Result<Object> getName(HttpServletRequest request) {
        String ipAddress = IpUtils.getIpAddress(request);
        String key = RedisPrefixConst.IP_ADDRESS_NICKNAME + "_" + ipAddress;
        if (redisService.hasKey(key)) {
            NicknameDTO nickNameDTO = (NicknameDTO) redisService.get(key);
            return Result.ok(nickNameDTO);
        }
        String randomName = IpUtils.getRandomName(ipAddress, NicknameConst.NicknameTeamEnum.jinyongNickname);
        NicknameDTO nickNameDTO = NicknameDTO.builder()
                .nickname(randomName)
                .count(0)
                .build();
        redisService.set(key, nickNameDTO, 60L * 60L * 12L);
        return Result.ok(nickNameDTO);
    }
}
