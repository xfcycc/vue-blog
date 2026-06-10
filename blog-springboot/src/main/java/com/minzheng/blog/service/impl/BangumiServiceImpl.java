package com.minzheng.blog.service.impl;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minzheng.blog.dao.BangumiDao;
import com.minzheng.blog.dto.BangumiDTO;
import com.minzheng.blog.entity.Bangumi;
import com.minzheng.blog.service.BangumiService;
import com.minzheng.blog.util.BeanCopyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.minzheng.blog.enums.ZoneEnum.SHANGHAI;

/**
 * B站追番服务
 *
 * @author caiguoyu
 * @date 2026/06/09
 */
@Service
@Slf4j
public class BangumiServiceImpl extends ServiceImpl<BangumiDao, Bangumi> implements BangumiService {

    private static final Integer BILIBILI_MID = 2883129;

    private static final Integer BANGUMI_TYPE = 1;

    private static final Integer PAGE_SIZE = 30;

    private static final String BILIBILI_FOLLOW_URL = "https://api.bilibili.com/x/space/bangumi/follow/list";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private BangumiDao bangumiDao;

    @Override
    public List<BangumiDTO> listBangumis() {
        List<Bangumi> bangumiList = bangumiDao.selectList(new LambdaQueryWrapper<Bangumi>()
                .orderByAsc(Bangumi::getSortOrder)
                .orderByDesc(Bangumi::getSyncTime));
        return BeanCopyUtils.copyList(bangumiList, BangumiDTO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void syncBangumis() {
        List<Bangumi> bangumiList = fetchBangumiList();
        if (bangumiList.isEmpty()) {
            log.warn("B站追番接口返回空列表，本次不更新本地数据");
            return;
        }
        this.saveOrUpdateBatch(bangumiList, PAGE_SIZE);
        List<Integer> seasonIdList = bangumiList.stream()
                .map(Bangumi::getSeasonId)
                .collect(Collectors.toList());
        this.remove(new LambdaQueryWrapper<Bangumi>().notIn(Bangumi::getSeasonId, seasonIdList));
        log.info("B站追番同步完成，共{}条", bangumiList.size());
    }

    private List<Bangumi> fetchBangumiList() {
        JSONObject firstPage = fetchBangumiPage(1);
        JSONObject data = firstPage.getJSONObject("data");
        Integer total = data.getInteger("total");
        if (total == null || total == 0) {
            return new ArrayList<>();
        }
        Integer pageCount = (total + PAGE_SIZE - 1) / PAGE_SIZE;
        List<Bangumi> bangumiList = new ArrayList<>();
        appendBangumis(bangumiList, data.getJSONArray("list"));
        for (int page = 2; page <= pageCount; page++) {
            JSONObject pageResult = fetchBangumiPage(page);
            appendBangumis(bangumiList, pageResult.getJSONObject("data").getJSONArray("list"));
        }
        return bangumiList.size() > total ? bangumiList.subList(0, total) : bangumiList;
    }

    private JSONObject fetchBangumiPage(Integer page) {
        String response = HttpRequest.get(BILIBILI_FOLLOW_URL)
                .form("type", BANGUMI_TYPE)
                .form("follow_status", 0)
                .form("pn", page)
                .form("ps", PAGE_SIZE)
                .form("vmid", BILIBILI_MID)
                .header("User-Agent", "Mozilla/5.0")
                .header("Referer", "https://space.bilibili.com/" + BILIBILI_MID + "/bangumi")
                .timeout(10000)
                .execute()
                .body();
        JSONObject result = JSON.parseObject(response);
        if (result == null || result.getInteger("code") == null || result.getInteger("code") != 0) {
            throw new IllegalStateException("B站追番接口请求失败：" + response);
        }
        return result;
    }

    private void appendBangumis(List<Bangumi> bangumiList, JSONArray itemList) {
        if (itemList == null || itemList.isEmpty()) {
            return;
        }
        LocalDateTime syncTime = LocalDateTime.now(ZoneId.of(SHANGHAI.getZone()));
        for (int i = 0; i < itemList.size(); i++) {
            JSONObject item = itemList.getJSONObject(i);
            bangumiList.add(toBangumi(item, bangumiList.size() + 1, syncTime));
        }
    }

    private Bangumi toBangumi(JSONObject item, Integer sortOrder, LocalDateTime syncTime) {
        JSONObject newEp = item.getJSONObject("new_ep");
        JSONObject rating = item.getJSONObject("rating");
        JSONObject publish = item.getJSONObject("publish");
        JSONObject stat = item.getJSONObject("stat");
        Long mediaId = item.getLong("media_id");
        return Bangumi.builder()
                .seasonId(item.getInteger("season_id"))
                .mediaId(mediaId)
                .sortOrder(sortOrder)
                .title(item.getString("title"))
                .seasonType(item.getInteger("season_type"))
                .seasonTypeName(item.getString("season_type_name"))
                .cover(toHttps(item.getString("cover")))
                .squareCover(toHttps(item.getString("square_cover")))
                .url(getBangumiUrl(item, mediaId))
                .progress(item.getString("progress"))
                .latestEpIndex(getString(newEp, "index_show"))
                .latestEpTitle(getString(newEp, "title"))
                .latestEpLongTitle(getString(newEp, "long_title"))
                .latestEpCover(toHttps(getString(newEp, "cover")))
                .latestEpPubTime(parseDateTime(getString(newEp, "pub_time")))
                .ratingScore(rating == null ? null : rating.getBigDecimal("score"))
                .ratingCount(getInteger(rating, "count"))
                .area(joinName(item.getJSONArray("areas")))
                .styles(joinName(item.getJSONArray("styles")))
                .isFinish(item.getInteger("is_finish"))
                .canWatch(item.getInteger("can_watch"))
                .followStatus(item.getInteger("follow_status"))
                .renewalTime(item.getString("renewal_time"))
                .pubTime(parseDateTime(getString(publish, "pub_time")))
                .releaseDate(getString(publish, "release_date_show"))
                .followCount(getLong(stat, "follow"))
                .viewCount(getLong(stat, "view"))
                .danmakuCount(getLong(stat, "danmaku"))
                .favoriteCount(getLong(stat, "favorite"))
                .evaluate(item.getString("evaluate"))
                .followTime(null)
                .syncTime(syncTime)
                .build();
    }

    private String getBangumiUrl(JSONObject item, Long mediaId) {
        String url = item.getString("url");
        if (StringUtils.hasText(url)) {
            return toHttps(url);
        }
        return mediaId == null ? "" : "https://www.bilibili.com/bangumi/media/md" + mediaId;
    }

    private String joinName(JSONArray array) {
        if (array == null || array.isEmpty()) {
            return "";
        }
        List<String> nameList = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JSONObject item = array.getJSONObject(i);
            String name = item.getString("name");
            if (StringUtils.hasText(name)) {
                nameList.add(name);
            }
        }
        return String.join("/", nameList);
    }

    private String toHttps(String url) {
        if (!StringUtils.hasText(url)) {
            return "";
        }
        return url.startsWith("http://") ? "https://" + url.substring("http://".length()) : url;
    }

    private LocalDateTime parseDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("B站追番时间解析失败：{}", value);
            return null;
        }
    }

    private String getString(JSONObject object, String key) {
        return object == null ? "" : object.getString(key);
    }

    private Integer getInteger(JSONObject object, String key) {
        return object == null ? null : object.getInteger(key);
    }

    private Long getLong(JSONObject object, String key) {
        return object == null ? null : object.getLong(key);
    }

}
