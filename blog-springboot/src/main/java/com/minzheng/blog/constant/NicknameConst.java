package com.minzheng.blog.constant;

import com.minzheng.blog.datamap.JingyongDataMap;
import com.minzheng.blog.datamap.impl.JingyongDataMapDefault;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wiremock.com.google.common.collect.Maps;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 花名大全
 *
 * @author caiguoyu
 * @date 2022/9/26
 */
@Component
public class NicknameConst {
    private static JingyongDataMap jingyongDataMap;

    private static final Map<String, String[]> TEAMS = new HashMap<>(Maps.newHashMapWithExpectedSize(10));

    @Autowired
    public NicknameConst(JingyongDataMap jingyongDataMap) {
        NicknameConst.jingyongDataMap = jingyongDataMap;
        TEAMS.put(NicknameTeamEnum.jinyongNickname.getName(), jingyongDataMap.defaultArray());
    }

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
