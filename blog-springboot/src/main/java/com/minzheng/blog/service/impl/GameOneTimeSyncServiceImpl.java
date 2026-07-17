package com.minzheng.blog.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minzheng.blog.dao.GameDao;
import com.minzheng.blog.dto.GameOneTimeSyncDTO;
import com.minzheng.blog.entity.Game;
import com.minzheng.blog.exception.BizException;
import com.minzheng.blog.service.GameOneTimeSyncService;
import com.minzheng.blog.service.GameService;
import com.minzheng.blog.vo.PsnOneTimeSyncVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * PSN一次性同步
 */
@Service
@Slf4j
public class GameOneTimeSyncServiceImpl implements GameOneTimeSyncService {

    private static final String SOURCE_PSN = "PSN";

    private static final String PLAYED_STATUS = "PLAYED";

    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");

    private static final int HTTP_TIMEOUT = 20000;

    private static final String PSN_AUTH_BASE_URL =
            "https://ca.account.sony.com/api/authz/v3/oauth";

    private static final String PSN_CLIENT_ID = "09515159-7237-4370-9b40-3806e67c0891";

    private static final String PSN_REDIRECT_URI = "com.scee.psxandroid.scecompcall://redirect";

    private static final String PSN_SCOPE = "psn:mobile.v2.core psn:clientapp";

    private static final String PSN_CLIENT_AUTHORIZATION =
            "Basic MDk1MTUxNTktNzIzNy00MzcwLTliNDAtMzgwNmU2N2MwODkxOnVjUGprYTV0bnRCMktxc1A=";

    private static final String PSN_GAMES_URL =
            "https://m.np.playstation.com/api/gamelist/v2/users/me/titles";

    @Resource
    private GameDao gameDao;

    @Resource
    private GameService gameService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public GameOneTimeSyncDTO syncPsnGames(PsnOneTimeSyncVO syncVO) {
        String npsso = normalizeNpsso(Objects.isNull(syncVO) ? null : syncVO.getNpsso());
        if (!StringUtils.hasText(npsso)) {
            throw new BizException("请粘贴PSN一次性授权码");
        }
        String accessCode = exchangePsnNpsso(npsso);
        String accessToken = exchangePsnAccessCode(accessCode);
        Map<String, SyncedGame> syncedGameMap = fetchPsnGames(accessToken);
        GameOneTimeSyncDTO result = persistSyncedGames(SOURCE_PSN, syncedGameMap);
        log.info("PSN游戏一次性同步完成，新增{}条，更新{}条，跳过{}条",
                result.getAddedCount(), result.getUpdatedCount(), result.getSkippedCount());
        return result;
    }

    private String normalizeNpsso(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String result = value.trim();
        if (result.startsWith("{")) {
            try {
                result = JSON.parseObject(result).getString("npsso");
            } catch (Exception ignored) {
                return null;
            }
        }
        return StringUtils.hasText(result) ? result.trim() : null;
    }

    private String exchangePsnNpsso(String npsso) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("access_type", "offline");
        params.put("client_id", PSN_CLIENT_ID);
        params.put("redirect_uri", PSN_REDIRECT_URI);
        params.put("response_type", "code");
        params.put("scope", PSN_SCOPE);
        HttpResponse response = HttpRequest.get(PSN_AUTH_BASE_URL + "/authorize?" + encodeParams(params))
                .header("Cookie", "npsso=" + npsso)
                .setFollowRedirects(false)
                .timeout(HTTP_TIMEOUT)
                .execute();
        String location = response.header("Location");
        if (!StringUtils.hasText(location)) {
            throw new BizException("PSN授权码无效，请重新登录后获取");
        }
        String query = URI.create(location).getRawQuery();
        String accessCode = parseParams(query).get("code");
        if (!StringUtils.hasText(accessCode)) {
            throw new BizException("PSN授权失败，请重新获取授权码");
        }
        return accessCode;
    }

    private String exchangePsnAccessCode(String accessCode) {
        HttpResponse response = HttpRequest.post(PSN_AUTH_BASE_URL + "/token")
                .header("Authorization", PSN_CLIENT_AUTHORIZATION)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .form("code", accessCode)
                .form("redirect_uri", PSN_REDIRECT_URI)
                .form("grant_type", "authorization_code")
                .form("token_format", "jwt")
                .timeout(HTTP_TIMEOUT)
                .execute();
        JSONObject body = parseResponseObject(response, "PSN登录失败");
        String accessToken = body.getString("access_token");
        if (!StringUtils.hasText(accessToken)) {
            throw new BizException("PSN登录失败");
        }
        return accessToken;
    }

    private Map<String, SyncedGame> fetchPsnGames(String accessToken) {
        Map<String, SyncedGame> result = new LinkedHashMap<>();
        int offset = 0;
        int total = Integer.MAX_VALUE;
        while (offset < total) {
            HttpResponse response = HttpRequest.get(PSN_GAMES_URL
                            + "?limit=200&offset=" + offset)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .timeout(HTTP_TIMEOUT)
                    .execute();
            JSONObject body = parseResponseObject(response, "PSN游戏记录获取失败");
            JSONArray titleArray = body.getJSONArray("titles");
            total = body.getIntValue("totalItemCount");
            if (titleArray == null || titleArray.isEmpty()) {
                break;
            }
            for (int i = 0; i < titleArray.size(); i++) {
                mergePsnGame(result, titleArray.getJSONObject(i));
            }
            int nextOffset = body.getIntValue("nextOffset");
            offset = nextOffset > offset ? nextOffset : offset + titleArray.size();
        }
        return result;
    }

    private void mergePsnGame(Map<String, SyncedGame> result, JSONObject item) {
        String titleId = item.getString("titleId");
        JSONObject concept = item.getJSONObject("concept");
        String sourceGameId = Objects.nonNull(concept) && Objects.nonNull(concept.get("id"))
                ? "CONCEPT_" + concept.getString("id") : titleId;
        String name = firstText(item.getString("localizedName"), item.getString("name"),
                Objects.isNull(concept) ? null : concept.getString("name"));
        if (!StringUtils.hasText(sourceGameId) || !StringUtils.hasText(name)) {
            return;
        }
        SyncedGame incoming = SyncedGame.builder()
                .sourceGameId(sourceGameId)
                .name(name)
                .cover(resolvePsnCover(item, concept))
                .playtimeSeconds(parseIsoDurationSeconds(item.getString("playDuration")))
                .lastPlayedTime(parseInstant(item.getString("lastPlayedDateTime")))
                .build();
        SyncedGame current = result.get(sourceGameId);
        if (Objects.isNull(current)) {
            result.put(sourceGameId, incoming);
            return;
        }
        current.setPlaytimeSeconds(current.getPlaytimeSeconds() + incoming.getPlaytimeSeconds());
        if (isAfter(incoming.getLastPlayedTime(), current.getLastPlayedTime())) {
            current.setName(incoming.getName());
            if (StringUtils.hasText(incoming.getCover())) {
                current.setCover(incoming.getCover());
            }
            current.setLastPlayedTime(incoming.getLastPlayedTime());
        }
    }

    private String resolvePsnCover(JSONObject item, JSONObject concept) {
        JSONObject media = item.getJSONObject("media");
        String screenshotUrl = Objects.isNull(media) ? null : media.getString("screenshotUrl");
        if (StringUtils.hasText(screenshotUrl)) {
            return screenshotUrl;
        }
        if (Objects.nonNull(concept)) {
            JSONObject conceptMedia = concept.getJSONObject("media");
            JSONArray imageArray = Objects.isNull(conceptMedia) ? null : conceptMedia.getJSONArray("images");
            if (imageArray != null && !imageArray.isEmpty()) {
                List<JSONObject> imageList = imageArray.toJavaList(JSONObject.class);
                imageList.sort(Comparator.comparingInt(image -> psnImagePriority(image.getString("type"))));
                String url = imageList.get(0).getString("url");
                if (StringUtils.hasText(url)) {
                    return url;
                }
            }
        }
        return firstText(item.getString("localizedImageUrl"), item.getString("imageUrl"));
    }

    private int psnImagePriority(String type) {
        if ("FOUR_BY_THREE_BANNER".equals(type)) return 0;
        if ("MASTER".equals(type)) return 1;
        return 2;
    }

    protected GameOneTimeSyncDTO persistSyncedGames(String source, Map<String, SyncedGame> syncedGameMap) {
        if (syncedGameMap.isEmpty()) {
            return GameOneTimeSyncDTO.builder()
                    .addedCount(0).updatedCount(0).skippedCount(0).build();
        }
        Map<String, Game> localGameMap = gameDao.selectList(new LambdaQueryWrapper<Game>()
                        .eq(Game::getSource, source))
                .stream()
                .filter(item -> StringUtils.hasText(item.getSourceGameId()))
                .collect(Collectors.toMap(Game::getSourceGameId, Function.identity(),
                        (left, right) -> left, LinkedHashMap::new));
        List<Game> addedList = new ArrayList<>();
        List<Game> updatedList = new ArrayList<>();
        int skippedCount = 0;
        LocalDateTime syncTime = LocalDateTime.now(SHANGHAI_ZONE);
        for (SyncedGame syncedGame : syncedGameMap.values()) {
            if (!StringUtils.hasText(syncedGame.getSourceGameId())
                    || !StringUtils.hasText(syncedGame.getName())) {
                skippedCount++;
                continue;
            }
            Game localGame = localGameMap.get(syncedGame.getSourceGameId());
            if (Objects.isNull(localGame)) {
                addedList.add(Game.builder()
                        .source(source)
                        .sourceGameId(syncedGame.getSourceGameId())
                        .gameName(syncedGame.getName())
                        .sourceCover(syncedGame.getCover())
                        .platforms(source)
                        .playStatus(PLAYED_STATUS)
                        .sortOrder(9999)
                        .isVisible(0)
                        .playtimeForever(toPlaytimeMinutes(syncedGame.getPlaytimeSeconds()))
                        .playtimeTwoWeeks(0)
                        .lastPlayedTime(syncedGame.getLastPlayedTime())
                        .syncTime(syncTime)
                        .build());
            } else {
                localGame.setGameName(syncedGame.getName());
                if (StringUtils.hasText(syncedGame.getCover())) {
                    localGame.setSourceCover(syncedGame.getCover());
                }
                if (syncedGame.getPlaytimeSeconds() != null && syncedGame.getPlaytimeSeconds() > 0) {
                    localGame.setPlaytimeForever(toPlaytimeMinutes(syncedGame.getPlaytimeSeconds()));
                }
                if (syncedGame.getLastPlayedTime() != null) {
                    localGame.setLastPlayedTime(syncedGame.getLastPlayedTime());
                }
                localGame.setSyncTime(syncTime);
                updatedList.add(localGame);
            }
        }
        if (!addedList.isEmpty()) {
            gameService.saveBatch(addedList, 100);
        }
        if (!updatedList.isEmpty()) {
            gameService.updateBatchById(updatedList, 100);
        }
        return GameOneTimeSyncDTO.builder()
                .addedCount(addedList.size())
                .updatedCount(updatedList.size())
                .skippedCount(skippedCount)
                .build();
    }

    private JSONObject parseResponseObject(HttpResponse response, String message) {
        if (Objects.isNull(response) || response.getStatus() < 200 || response.getStatus() >= 300) {
            throw new BizException(message);
        }
        try {
            JSONObject body = JSON.parseObject(response.body());
            if (Objects.isNull(body) || body.containsKey("error") || body.containsKey("errorCode")) {
                throw new BizException(message);
            }
            return body;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(message);
        }
    }

    private Map<String, String> parseParams(String value) {
        if (!StringUtils.hasText(value)) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (String item : value.split("&")) {
            String[] pair = item.split("=", 2);
            if (pair.length > 0) {
                result.put(urlDecode(pair[0]), pair.length > 1 ? urlDecode(pair[1]) : "");
            }
        }
        return result;
    }

    private String encodeParams(Map<String, String> params) {
        return params.entrySet().stream()
                .map(item -> urlEncode(item.getKey()) + "=" + urlEncode(item.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name()).replace("+", "%20");
        } catch (Exception e) {
            throw new BizException("授权参数生成失败");
        }
    }

    private String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return value;
        }
    }

    private long parseIsoDurationSeconds(String value) {
        if (!StringUtils.hasText(value)) {
            return 0L;
        }
        try {
            return Math.max(0L, Duration.parse(value).getSeconds());
        } catch (DateTimeParseException ignored) {
            return 0L;
        }
    }

    private int toPlaytimeMinutes(Long seconds) {
        if (Objects.isNull(seconds) || seconds <= 0) {
            return 0;
        }
        return (int) Math.min(Integer.MAX_VALUE, seconds / 60);
    }

    private LocalDateTime parseInstant(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDateTime.ofInstant(Instant.parse(value), SHANGHAI_ZONE);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private boolean isAfter(LocalDateTime left, LocalDateTime right) {
        return left != null && (right == null || left.isAfter(right));
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    private static class SyncedGame {
        private String sourceGameId;
        private String name;
        private String cover;
        private Long playtimeSeconds;
        private LocalDateTime lastPlayedTime;
    }
}
