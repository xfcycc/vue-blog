package com.minzheng.blog.util;

import com.minzheng.blog.dto.VisitorAvatarDTO;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

/**
 * 头像工具类
 *
 * @author caiguoyu
 * @date 2026/05/18
 */
public class AvatarUtils {

    private static final int POKEMON_AVATAR_COUNT = 151;

    private AvatarUtils() {
    }

    public static VisitorAvatarDTO getVisitorAvatar(HttpServletRequest request, String macAddress) {
        String ipAddress = IpUtils.getIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String seed = getVisitorSeed(ipAddress, userAgent, macAddress);
        int avatarId = getAvatarId(seed);
        return VisitorAvatarDTO.builder()
                .avatarId(avatarId)
                .avatar(getAvatarUrl(avatarId))
                .build();
    }

    public static VisitorAvatarDTO getVisitorAvatar(HttpServletRequest request) {
        return getVisitorAvatar(request, null);
    }

    public static String getAvatarUrl(int avatarId) {
        return String.format("/pokemon-avatars/%03d.webp", avatarId);
    }

    public static String getAvatarUrlBySeed(String seed) {
        return getAvatarUrl(getAvatarId(seed));
    }

    public static boolean isPokemonAvatar(String avatar) {
        return avatar != null && avatar.startsWith("/pokemon-avatars/");
    }

    public static String getVisitorKey(String ipAddress, String userAgent) {
        return DigestUtils.md5DigestAsHex(getVisitorSeed(ipAddress, userAgent, null).getBytes(StandardCharsets.UTF_8));
    }

    private static String getVisitorSeed(String ipAddress, String userAgent, String macAddress) {
        return (ipAddress == null ? "" : ipAddress)
                + "|"
                + (macAddress == null ? "" : macAddress)
                + "|"
                + (userAgent == null ? "" : userAgent);
    }

    private static int getAvatarId(String seed) {
        byte[] digest = DigestUtils.md5Digest(seed.getBytes(StandardCharsets.UTF_8));
        int hash = 0;
        for (byte item : digest) {
            hash = (hash << 5) - hash + (item & 0xff);
        }
        return Math.floorMod(hash, POKEMON_AVATAR_COUNT) + 1;
    }
}
