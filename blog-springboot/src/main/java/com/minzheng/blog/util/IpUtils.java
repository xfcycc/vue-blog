package com.minzheng.blog.util;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.minzheng.blog.constant.NicknameConst;
import com.minzheng.blog.constant.RedisPrefixConst;
import com.minzheng.blog.dto.NicknameDTO;
import com.minzheng.blog.service.RedisService;
import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ip工具类
 *
 * @author 11921
 */
@Component
@SuppressWarnings("all")
public class IpUtils {

    private static RedisService redisService;

    @Autowired
    public IpUtils(RedisService redisService) {
        this.redisService = redisService;
    }

    /**
     * 获取用户ip地址
     *
     * @param request 请求
     * @return ip地址
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress = null;
        try {
            ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if ("127.0.0.1".equals(ipAddress)) {
                    // 根据网卡取本机配置的IP
                    InetAddress inet = null;
                    try {
                        inet = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    ipAddress = inet.getHostAddress();
                }
            }
            // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            if (ipAddress != null && ipAddress.length() > 15) {
                // = 15
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
        } catch (Exception e) {
            ipAddress = "";
        }
        return ipAddress;
    }

    /**
     * 解析ip地址
     *
     * @param ipAddress ip地址
     * @return 解析后的ip地址
     */
    public static String getIpSource(String ipAddress) {
        if (redisService.hHasKey(RedisPrefixConst.IP_ADDRESS_SOURCE, ipAddress)) {
            return (String) redisService.hGet(RedisPrefixConst.IP_ADDRESS_SOURCE, ipAddress);
        }
        try {
            URL url = new URL("http://opendata.baidu.com/api.php?query=" + ipAddress + "&co=&resource_id=6006&oe=utf8");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream(), "utf-8"));
            String line = null;
            StringBuffer result = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();
            Map map = JSON.parseObject(result.toString(), Map.class);
            List<Map<String, String>> data = (List) map.get("data");
            String location = data.get(0).get("location");
            redisService.hSet(RedisPrefixConst.IP_ADDRESS_SOURCE, ipAddress, location, 60L * 60L);
            return location;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取访问设备
     *
     * @param request 请求
     * @return {@link UserAgent} 访问设备
     */
    public static UserAgent getUserAgent(HttpServletRequest request) {
        return UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
    }

    /**
     * 根据ip生成匿名，12小时有效
     *
     * @param ipAddress 全地址
     * @param team      组名
     * @return java.lang.String
     * @author caiguoyu
     * @date 2022/9/26
     */
    public static String getRandomName(String ipAddress, NicknameConst.NicknameTeamEnum team) {
        String ip = Objects.equals("未知ip", ipAddress) ? "127.0.0.1" : ipAddress;
        String key = RedisPrefixConst.IP_ADDRESS_NICKNAME + "_" + ip;
        NicknameDTO value = new NicknameDTO();
        String[] nicknameTeam = NicknameConst.findTeamFactory(team);
        if (redisService.hasKey(key)) {
            NicknameDTO nickNameDTO = (NicknameDTO) redisService.get(key);
            return nickNameDTO.getNickname();
        }
        int r = RandomUtil.getRandom().nextInt(0, nicknameTeam.length);
        value = NicknameDTO.builder()
                .nickname(nicknameTeam[r])
                .count(1)
                .build();
        redisService.set(key, value, 60L * 60L * 12L);
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
    public static String refreshRandomName(String ipAddress, NicknameConst.NicknameTeamEnum team) {
        String key = RedisPrefixConst.IP_ADDRESS_NICKNAME + "_" + ipAddress;
        String[] nicknameTeam = NicknameConst.findTeamFactory(team);
        if (redisService.hasKey(key)) {
            NicknameDTO nickNameDTO = (NicknameDTO) redisService.get(key);
            Integer count = nickNameDTO.getCount();
            if (count > 3) {
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
            redisService.set(key, nickNameDTO, 60L * 60L * 12L);
            return nickNameDTO.getNickname();
        } else {
            String randomName = getRandomName(ipAddress, team);
            return randomName;
        }

    }
}
