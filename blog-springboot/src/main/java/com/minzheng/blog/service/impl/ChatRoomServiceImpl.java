package com.minzheng.blog.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.minzheng.blog.constant.NicknameConstant;
import com.minzheng.blog.constant.RedisPrefixConst;
import com.minzheng.blog.dictionary.NicknameDictionary;
import com.minzheng.blog.dto.NicknameDTO;
import com.minzheng.blog.service.ChatRoomService;
import com.minzheng.blog.service.RedisService;
import com.minzheng.blog.util.IpUtils;
import com.minzheng.blog.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author caiguoyu
 * @date 2022/9/26
 */
@Service
public class ChatRoomServiceImpl implements ChatRoomService {

    private static RedisService redisService;

    @Autowired
    public ChatRoomServiceImpl(RedisService redisService) {
        ChatRoomServiceImpl.redisService = redisService;
    }

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
        if (NicknameDictionary.findEnum(team) == null) {
            return Result.fail(404, "分类未知");
        }
        String name = refreshRandomName(ipAddress, NicknameDictionary.findEnum(team));
        if (StringUtils.isBlank(name)) {
            return Result.fail(203, "刷新次数已达上限，请明天再试！");
        }
        String key = RedisPrefixConst.IP_ADDRESS_NICKNAME + "_" + ipAddress;
        NicknameDTO nickNameDTO = (NicknameDTO) redisService.hGet(RedisPrefixConst.IP_ADDRESS_NICKNAME, key);
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
        if (redisService.hHasKey(RedisPrefixConst.IP_ADDRESS_NICKNAME, key)) {
            NicknameDTO nickNameDTO = (NicknameDTO) redisService.hGet(RedisPrefixConst.IP_ADDRESS_NICKNAME, key);
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
        if (redisService.hHasKey(RedisPrefixConst.IP_ADDRESS_NICKNAME, key)) {
            NicknameDTO nickNameDTO = (NicknameDTO) redisService.hGet(RedisPrefixConst.IP_ADDRESS_NICKNAME, key);
            return Result.ok(nickNameDTO);
        }
        String randomName = getRandomName(ipAddress, NicknameDictionary.NicknameTeamEnum.jinyongNickname);
        NicknameDTO nickNameDTO = NicknameDTO.builder()
                .nickname(randomName)
                .count(0)
                .remainCount(NicknameConstant.MAX_DAILY_REFRESH_COUNT)
                .build();
        // 由定时任务每日清除
        redisService.hSet(RedisPrefixConst.IP_ADDRESS_NICKNAME, key, nickNameDTO, 60L * 60L * 24L);
        return Result.ok(nickNameDTO);
    }

    /**
     * 根据ip生成匿名，24小时有效
     *
     * @param ipAddress 全地址
     * @param team      组名
     * @return java.lang.String
     * @author caiguoyu
     * @date 2022/9/26
     */
    @Override
    public String getRandomName(String ipAddress, NicknameDictionary.NicknameTeamEnum team) {
        String ip = Objects.equals("未知ip", ipAddress) ? "127.0.0.1" : ipAddress;
        String key = RedisPrefixConst.IP_ADDRESS_NICKNAME + "_" + ip;
        NicknameDTO value = new NicknameDTO();
        String[] nicknameTeam = NicknameDictionary.findTeamFactory(team);
        if (redisService.hHasKey(RedisPrefixConst.IP_ADDRESS_NICKNAME, key)) {
            NicknameDTO nickNameDTO = (NicknameDTO) redisService.hGet(RedisPrefixConst.IP_ADDRESS_NICKNAME, key);
            return nickNameDTO.getNickname();
        }
        int r = RandomUtil.getRandom().nextInt(0, nicknameTeam.length);
        value = NicknameDTO.builder()
                .nickname(nicknameTeam[r])
                .count(1)
                .remainCount(NicknameConstant.MAX_DAILY_REFRESH_COUNT)
                .build();
        redisService.hSet(RedisPrefixConst.IP_ADDRESS_NICKNAME, key, value, 60L * 60L * 24L);
        return value.getNickname();
    }

    /**
     * 刷新匿名
     *
     * @param ipAddress 全地址
     * @return java.lang.String
     * @author caiguoyu
     * @date 2022/9/26
     */
    public String refreshRandomName(String ipAddress, NicknameDictionary.NicknameTeamEnum team) {
        String key = RedisPrefixConst.IP_ADDRESS_NICKNAME + "_" + ipAddress;
        String[] nicknameTeam = NicknameDictionary.findTeamFactory(team);
        if (redisService.hHasKey(RedisPrefixConst.IP_ADDRESS_NICKNAME, key)) {
            NicknameDTO nickNameDTO = (NicknameDTO) redisService.hGet(RedisPrefixConst.IP_ADDRESS_NICKNAME, key);
            Integer count = nickNameDTO.getCount();
            if (count >= NicknameConstant.MAX_DAILY_REFRESH_COUNT) {
                return "";
            }
            int r = RandomUtil.getRandom().nextInt(0, nicknameTeam.length);
            while (nicknameTeam[r].equals(nickNameDTO.getNickname())) {
                if (r == 0) {
                    r++;
                } else if (r == nicknameTeam.length) {
                    r--;
                } else {
                    r = RandomUtil.getRandom().nextInt(0, nicknameTeam.length);
                }
            }
            nickNameDTO.setNickname(nicknameTeam[r]);
            nickNameDTO.setCount(++count);
            nickNameDTO.setRemainCount(NicknameConstant.MAX_DAILY_REFRESH_COUNT - count);
            redisService.hSet(RedisPrefixConst.IP_ADDRESS_NICKNAME, key, nickNameDTO, 60L * 60L * 24L);
            return nickNameDTO.getNickname();
        } else {
            return getRandomName(ipAddress, team);
        }

    }
}
