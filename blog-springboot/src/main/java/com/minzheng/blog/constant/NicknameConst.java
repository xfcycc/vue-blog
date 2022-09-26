package com.minzheng.blog.constant;

import lombok.Getter;
import wiremock.com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;

/**
 * 花名大全
 *
 * @author caiguoyu
 * @date 2022/9/26
 */
public class NicknameConst {
    @Getter
    public enum NicknameTeamEnum {
        /**
         * 金庸组
         */
        jinyongNickname("jinyongNickname"),
        ;

        private String name;

        NicknameTeamEnum(String name) {
            this.name = name;
        }
    }

    private static final String[] JINYONG_NICKNAME = {"风清扬", "东方不败", "令狐冲", "岳不群",
            "乔峰", "韦小宝", "杨过", "小龙女", "郭靖", "黄蓉"};



    private static final Map<String, String[]> TEAMS = new HashMap<>(Maps.newHashMapWithExpectedSize(10));

    static {
        TEAMS.put(NicknameTeamEnum.jinyongNickname.getName(), JINYONG_NICKNAME);
    }
    /**
     * 根据使用匿名组别返回各自的花名组
     *
     * @param team 组名
     * @return java.lang.String[]
     * @author caiguoyu
     * @date 2022/9/26
     */
    public static String[] findTeamFactory(NicknameTeamEnum team) {
        if (!TEAMS.containsKey(team.getName())) {
            return TEAMS.get(NicknameTeamEnum.jinyongNickname.getName());
        }
        return TEAMS.get(team.getName());
    }

    /**
     * 匹配组别
     *
     * @param team 组名
     * @author caiguoyu
     * @date 2022/9/26
     */
    public static NicknameTeamEnum findEnum(String team) {
        if (team.equals(NicknameTeamEnum.jinyongNickname.getName())) {
            return NicknameTeamEnum.jinyongNickname;
        }
        return null;
    }
}
