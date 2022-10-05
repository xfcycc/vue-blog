package com.minzheng.blog.dictionary;

import com.minzheng.blog.constant.NicknameConstant;
import com.minzheng.blog.datamap.JingyongDataMap;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wiremock.com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;

/**
 * 花名大全
 *
 * @author caiguoyu
 * @date 2022/9/26
 */
@Component
public class NicknameDictionary {
    private static JingyongDataMap jingyongDataMap;

    private static final Map<String, String[]> TEAMS = new HashMap<>(Maps.newHashMapWithExpectedSize(10));

    @Autowired
    public NicknameDictionary(JingyongDataMap jingyongDataMap) {
        NicknameDictionary.jingyongDataMap = jingyongDataMap;
        TEAMS.put(NicknameTeamEnum.jinyongNickname.getName(), jingyongDataMap.defaultArray());
    }

    @Getter
    public enum NicknameTeamEnum {
        /**
         * 金庸组
         */
        jinyongNickname(NicknameConstant.JINYONG_NICKNAME),
        ;

        private String name;

        NicknameTeamEnum(String name) {
            this.name = name;
        }
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
            return jingyongDataMap.defaultArray();
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
