package com.minzheng.blog.service.impl;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minzheng.blog.dao.GameDao;
import com.minzheng.blog.dao.GameFieldDao;
import com.minzheng.blog.dao.GameScreenshotDao;
import com.minzheng.blog.dto.GameBackDTO;
import com.minzheng.blog.dto.GameDTO;
import com.minzheng.blog.dto.GameDeleteDTO;
import com.minzheng.blog.dto.GameDetailDTO;
import com.minzheng.blog.dto.GameFieldDTO;
import com.minzheng.blog.dto.GameListDTO;
import com.minzheng.blog.dto.GameScreenshotBackDTO;
import com.minzheng.blog.dto.GameScreenshotDTO;
import com.minzheng.blog.dto.SteamSyncDTO;
import com.minzheng.blog.entity.Game;
import com.minzheng.blog.entity.GameField;
import com.minzheng.blog.entity.GameScreenshot;
import com.minzheng.blog.exception.BizException;
import com.minzheng.blog.service.GameConfigService;
import com.minzheng.blog.service.GameFieldService;
import com.minzheng.blog.service.GameService;
import com.minzheng.blog.service.GameScreenshotService;
import com.minzheng.blog.util.BeanCopyUtils;
import com.minzheng.blog.vo.GameBackQueryVO;
import com.minzheng.blog.vo.GameDeleteVO;
import com.minzheng.blog.vo.GameFieldVO;
import com.minzheng.blog.vo.GameIdVO;
import com.minzheng.blog.vo.GameQueryVO;
import com.minzheng.blog.vo.GameSaveVO;
import com.minzheng.blog.vo.GameScreenshotSaveVO;
import com.minzheng.blog.vo.PageResult;
import com.minzheng.blog.vo.SteamSyncVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 游戏档案服务
 *
 * @author caiguoyu
 * @date 2026/07/17
 */
@Service
@Slf4j
public class GameServiceImpl extends ServiceImpl<GameDao, Game> implements GameService {

    private static final String SOURCE_STEAM = "STEAM";

    private static final String SOURCE_CUSTOM = "CUSTOM";

    private static final String DEFAULT_STATUS = "WANT";

    private static final String PLAYED_STATUS = "PLAYED";

    private static final String STEAM_API_KEY_CONFIG = "STEAM_WEB_API_KEY";

    private static final String STEAM_ID_CONFIG = "STEAM_ID";

    private static final String STEAM_OWNED_GAMES_URL =
            "https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/";

    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Set<String> PLATFORM_SET = new LinkedHashSet<>(Arrays.asList(
            "STEAM", "PSN", "PC", "MOBILE", "SWITCH"
    ));

    private static final Set<String> PLAY_STATUS_SET = new LinkedHashSet<>(Arrays.asList(
            "WANT", "PLAYING", PLAYED_STATUS
    ));

    private static final Set<String> FIELD_TYPE_SET = new LinkedHashSet<>(Arrays.asList(
            "TEXT", "NUMBER", "CURRENCY", "PERCENT", "DATE", "LINK"
    ));

    private static final Set<String> SCREENSHOT_LAYOUT_SET = new LinkedHashSet<>(Arrays.asList(
            "CAROUSEL", "FEATURED", "GRID", "CUSTOM"
    ));

    private static final Set<String> SCREENSHOT_FRAME_SET = new LinkedHashSet<>(Arrays.asList(
            "AUTO", "LANDSCAPE", "PORTRAIT", "SQUARE"
    ));

    private static final Set<Integer> SCREENSHOT_COLUMN_SPAN_SET = new LinkedHashSet<>(Arrays.asList(
            4, 6, 8, 12
    ));

    @Resource
    private GameDao gameDao;

    @Resource
    private GameFieldDao gameFieldDao;

    @Resource
    private GameFieldService gameFieldService;

    @Resource
    private GameScreenshotDao gameScreenshotDao;

    @Resource
    private GameScreenshotService gameScreenshotService;

    @Resource
    private GameConfigService gameConfigService;

    @Override
    public GameListDTO listGames(GameQueryVO queryVO) {
        GameQueryVO query = Objects.isNull(queryVO) ? new GameQueryVO() : queryVO;
        List<Game> visibleGameList = gameDao.selectList(new LambdaQueryWrapper<Game>()
                .select(Game::getPlayStatus, Game::getPlatforms)
                .eq(Game::getIsVisible, 1));
        Map<String, Integer> statusCountMap = visibleGameList.stream()
                .filter(item -> StringUtils.hasText(item.getPlayStatus()))
                .collect(Collectors.groupingBy(item -> normalizePlayStatus(item.getPlayStatus()),
                        LinkedHashMap::new, Collectors.summingInt(item -> 1)));
        List<String> platformList = visibleGameList.stream()
                .flatMap(item -> splitPlatforms(item.getPlatforms()).stream())
                .distinct()
                .collect(Collectors.toList());

        long current = normalizePage(query.getCurrent(), 1L);
        long size = normalizePage(query.getSize(), 8L);
        LambdaQueryWrapper<Game> wrapper = new LambdaQueryWrapper<Game>()
                .eq(Game::getIsVisible, 1)
                .like(StringUtils.hasText(query.getPlatform()), Game::getPlatforms, normalizePlatform(query.getPlatform()))
                .orderByAsc(Game::getSortOrder)
                .orderByDesc(Game::getLastPlayedTime)
                .orderByDesc(Game::getId);
        appendGameNameCondition(wrapper, query.getKeywords());
        appendPlayStatusCondition(wrapper, query.getPlayStatus());
        Page<Game> gamePage = gameDao.selectPage(new Page<>(current, size), wrapper);
        List<GameDTO> gameDTOList = convertGameList(gamePage.getRecords());
        bindCardFields(gameDTOList);
        return GameListDTO.builder()
                .recordList(gameDTOList)
                .count((int) gamePage.getTotal())
                .statusCountMap(statusCountMap)
                .platformList(platformList)
                .build();
    }

    @Override
    public GameDetailDTO getGameDetail(GameIdVO gameIdVO, boolean admin) {
        if (Objects.isNull(gameIdVO) || Objects.isNull(gameIdVO.getId())) {
            throw new BizException("游戏不存在");
        }
        LambdaQueryWrapper<Game> wrapper = new LambdaQueryWrapper<Game>()
                .eq(Game::getId, gameIdVO.getId())
                .eq(!admin, Game::getIsVisible, 1);
        Game game = gameDao.selectOne(wrapper);
        if (Objects.isNull(game)) {
            throw new BizException("游戏不存在或未公开");
        }
        List<GameField> fieldList = gameFieldDao.selectList(new LambdaQueryWrapper<GameField>()
                .eq(GameField::getGameId, game.getId())
                .orderByAsc(GameField::getSortOrder)
                .orderByAsc(GameField::getId));
        List<GameScreenshot> screenshotList = gameScreenshotDao.selectList(
                new LambdaQueryWrapper<GameScreenshot>()
                        .eq(GameScreenshot::getGameId, game.getId())
                        .orderByAsc(GameScreenshot::getSortOrder)
                        .orderByAsc(GameScreenshot::getId));
        return convertGameDetail(game, fieldList, screenshotList, admin);
    }

    @Override
    public PageResult<GameBackDTO> listBackGames(GameBackQueryVO queryVO) {
        GameBackQueryVO query = Objects.isNull(queryVO) ? new GameBackQueryVO() : queryVO;
        long current = normalizePage(query.getCurrent(), 1L);
        long size = normalizePage(query.getSize(), 10L);
        LambdaQueryWrapper<Game> wrapper = new LambdaQueryWrapper<Game>()
                .eq(StringUtils.hasText(query.getSource()), Game::getSource, query.getSource())
                .eq(Objects.nonNull(query.getIsVisible()), Game::getIsVisible, query.getIsVisible())
                .like(StringUtils.hasText(query.getPlatform()), Game::getPlatforms, normalizePlatform(query.getPlatform()))
                .orderByAsc(Game::getSortOrder)
                .orderByDesc(Game::getUpdateTime)
                .orderByDesc(Game::getId);
        appendGameNameCondition(wrapper, query.getKeywords());
        appendPlayStatusCondition(wrapper, query.getPlayStatus());
        Page<Game> gamePage = gameDao.selectPage(new Page<>(current, size), wrapper);
        List<GameBackDTO> recordList = gamePage.getRecords().stream()
                .map(this::convertGameBack)
                .collect(Collectors.toList());
        return new PageResult<>(recordList, (int) gamePage.getTotal());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer saveOrUpdateGame(GameSaveVO gameSaveVO) {
        if (Objects.isNull(gameSaveVO)) {
            throw new BizException("游戏信息不能为空");
        }
        Game original = Objects.isNull(gameSaveVO.getId()) ? null : gameDao.selectById(gameSaveVO.getId());
        if (Objects.nonNull(gameSaveVO.getId()) && Objects.isNull(original)) {
            throw new BizException("游戏不存在");
        }
        Game game = buildSavedGame(gameSaveVO, original);
        this.saveOrUpdate(game);
        saveGameFields(game.getId(), gameSaveVO.getFieldList());
        saveGameScreenshots(game.getId(), gameSaveVO);
        return game.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public GameDeleteDTO deleteGames(GameDeleteVO deleteVO) {
        if (Objects.isNull(deleteVO) || deleteVO.getIdList() == null || deleteVO.getIdList().isEmpty()) {
            return GameDeleteDTO.builder().deletedCount(0).skippedCount(0).build();
        }
        List<Game> gameList = gameDao.selectList(new LambdaQueryWrapper<Game>()
                .select(Game::getId, Game::getSource)
                .in(Game::getId, deleteVO.getIdList()));
        List<Integer> customIdList = gameList.stream()
                .filter(item -> SOURCE_CUSTOM.equals(item.getSource()))
                .map(Game::getId)
                .collect(Collectors.toList());
        if (!customIdList.isEmpty()) {
            gameFieldService.remove(new LambdaQueryWrapper<GameField>().in(GameField::getGameId, customIdList));
            gameScreenshotService.remove(new LambdaQueryWrapper<GameScreenshot>()
                    .in(GameScreenshot::getGameId, customIdList));
            this.removeByIds(customIdList);
        }
        return GameDeleteDTO.builder()
                .deletedCount(customIdList.size())
                .skippedCount(gameList.size() - customIdList.size())
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SteamSyncDTO syncSteamGames(SteamSyncVO syncVO) {
        Map<String, String> steamConfig = gameConfigService.getPlatformConfig(SOURCE_STEAM);
        String steamApiKey = steamConfig.get(STEAM_API_KEY_CONFIG);
        String steamId = steamConfig.get(STEAM_ID_CONFIG);
        if (!StringUtils.hasText(steamApiKey) || !StringUtils.hasText(steamId)) {
            throw new BizException("未配置STEAM_WEB_API_KEY或STEAM_ID");
        }
        boolean includeFreeGames = Objects.isNull(syncVO)
                || !Boolean.FALSE.equals(syncVO.getIncludePlayedFreeGames());
        JSONArray steamGameArray = fetchSteamGames(includeFreeGames, steamApiKey, steamId);
        if (steamGameArray == null || steamGameArray.isEmpty()) {
            log.warn("Steam游戏库返回空列表，本次保留本地数据");
            return SteamSyncDTO.builder().addedCount(0).updatedCount(0).skippedCount(0).build();
        }
        Map<String, Game> localGameMap = gameDao.selectList(new LambdaQueryWrapper<Game>()
                        .eq(Game::getSource, SOURCE_STEAM))
                .stream()
                .filter(item -> Objects.nonNull(item.getSourceGameId()))
                .collect(Collectors.toMap(Game::getSourceGameId, Function.identity(), (left, right) -> left));
        List<Game> addedList = new ArrayList<>();
        List<Game> updatedList = new ArrayList<>();
        int skippedCount = 0;
        LocalDateTime syncTime = LocalDateTime.now(SHANGHAI_ZONE);
        for (int i = 0; i < steamGameArray.size(); i++) {
            JSONObject item = steamGameArray.getJSONObject(i);
            String appId = item.getString("appid");
            if (Objects.isNull(appId) || !StringUtils.hasText(item.getString("name"))) {
                skippedCount++;
                continue;
            }
            Game localGame = localGameMap.get(appId);
            if (Objects.isNull(localGame)) {
                addedList.add(buildSteamGame(item, syncTime));
            } else {
                refreshSteamGame(localGame, item, syncTime);
                updatedList.add(localGame);
            }
        }
        if (!addedList.isEmpty()) {
            this.saveBatch(addedList, 100);
        }
        if (!updatedList.isEmpty()) {
            this.updateBatchById(updatedList, 100);
        }
        log.info("Steam游戏同步完成，新增{}条，更新{}条，跳过{}条",
                addedList.size(), updatedList.size(), skippedCount);
        return SteamSyncDTO.builder()
                .addedCount(addedList.size())
                .updatedCount(updatedList.size())
                .skippedCount(skippedCount)
                .build();
    }

    private void bindCardFields(List<GameDTO> gameDTOList) {
        if (gameDTOList.isEmpty()) {
            return;
        }
        List<Integer> gameIdList = gameDTOList.stream().map(GameDTO::getId).collect(Collectors.toList());
        Map<Integer, List<GameFieldDTO>> fieldMap = gameFieldDao.selectList(new LambdaQueryWrapper<GameField>()
                        .in(GameField::getGameId, gameIdList)
                        .eq(GameField::getShowOnCard, 1)
                        .orderByAsc(GameField::getSortOrder)
                        .orderByAsc(GameField::getId))
                .stream()
                .collect(Collectors.groupingBy(GameField::getGameId, LinkedHashMap::new,
                        Collectors.mapping(this::convertGameField, Collectors.toList())));
        gameDTOList.forEach(item -> item.setCardFieldList(fieldMap
                .getOrDefault(item.getId(), Collections.emptyList())
                .stream()
                .limit(3)
                .collect(Collectors.toList())));
    }

    private List<GameDTO> convertGameList(List<Game> gameList) {
        return gameList.stream().map(game -> GameDTO.builder()
                .id(game.getId())
                .source(game.getSource())
                .gameName(resolveGameName(game))
                .gameIntro(game.getGameIntro())
                .cover(resolveCover(game))
                .gameUrl(game.getGameUrl())
                .platformList(splitPlatforms(game.getPlatforms()))
                .tagList(splitTags(game.getTags()))
                .playStatus(normalizePlayStatus(game.getPlayStatus()))
                .personalScore(game.getPersonalScore())
                .playtimeForever(game.getPlaytimeForever())
                .playtimeTwoWeeks(game.getPlaytimeTwoWeeks())
                .lastPlayedTime(game.getLastPlayedTime())
                .cardFieldList(new ArrayList<>())
                .build()).collect(Collectors.toList());
    }

    private GameDetailDTO convertGameDetail(Game game, List<GameField> fieldList,
                                            List<GameScreenshot> screenshotList, boolean admin) {
        List<String> legacyScreenshotList = splitScreenshots(game.getScreenshots());
        return GameDetailDTO.builder()
                .id(game.getId())
                .source(game.getSource())
                .sourceGameId(game.getSourceGameId())
                .gameName(admin ? game.getGameName() : resolveGameName(game))
                .gameAlias(game.getGameAlias())
                .gameIntro(game.getGameIntro())
                .cover(resolveCover(game))
                .sourceCover(game.getSourceCover())
                .customCover(game.getCustomCover())
                .gameUrl(game.getGameUrl())
                .platformList(splitPlatforms(game.getPlatforms()))
                .tagList(splitTags(game.getTags()))
                .screenshotList(resolveDisplayScreenshotList(screenshotList, legacyScreenshotList))
                .screenshotItemList(convertGameScreenshots(screenshotList, legacyScreenshotList, admin))
                .screenshotLayout(normalizeScreenshotLayout(game.getScreenshotLayout()))
                .playStatus(normalizePlayStatus(game.getPlayStatus()))
                .personalScore(game.getPersonalScore())
                .sortOrder(game.getSortOrder())
                .isVisible(game.getIsVisible())
                .playtimeForever(game.getPlaytimeForever())
                .playtimeTwoWeeks(game.getPlaytimeTwoWeeks())
                .lastPlayedTime(game.getLastPlayedTime())
                .reviewContent(game.getReviewContent())
                .syncTime(game.getSyncTime())
                .fieldList(fieldList.stream().map(this::convertGameField).collect(Collectors.toList()))
                .build();
    }

    private GameBackDTO convertGameBack(Game game) {
        return GameBackDTO.builder()
                .id(game.getId())
                .source(game.getSource())
                .sourceGameId(game.getSourceGameId())
                .gameName(resolveGameName(game))
                .cover(resolveCover(game))
                .platformList(splitPlatforms(game.getPlatforms()))
                .playStatus(normalizePlayStatus(game.getPlayStatus()))
                .personalScore(game.getPersonalScore())
                .sortOrder(game.getSortOrder())
                .isVisible(game.getIsVisible())
                .playtimeForever(game.getPlaytimeForever())
                .lastPlayedTime(game.getLastPlayedTime())
                .syncTime(game.getSyncTime())
                .build();
    }

    private GameFieldDTO convertGameField(GameField gameField) {
        return BeanCopyUtils.copyObject(gameField, GameFieldDTO.class);
    }

    private Game buildSavedGame(GameSaveVO gameSaveVO, Game original) {
        if (Objects.isNull(original)) {
            return Game.builder()
                    .source(SOURCE_CUSTOM)
                    .gameName(gameSaveVO.getGameName())
                    .gameAlias(gameSaveVO.getGameAlias())
                    .gameIntro(gameSaveVO.getGameIntro())
                    .customCover(gameSaveVO.getCustomCover())
                    .gameUrl(gameSaveVO.getGameUrl())
                    .platforms(joinPlatforms(gameSaveVO.getPlatformList(), null))
                    .tags(joinTags(gameSaveVO.getTagList()))
                    .screenshots(joinScreenshots(resolveSubmittedScreenshotList(gameSaveVO)))
                    .screenshotLayout(resolveSubmittedScreenshotLayout(gameSaveVO))
                    .playStatus(normalizePlayStatus(gameSaveVO.getPlayStatus()))
                    .personalScore(gameSaveVO.getPersonalScore())
                    .sortOrder(defaultInteger(gameSaveVO.getSortOrder(), 9999))
                    .isVisible(defaultInteger(gameSaveVO.getIsVisible(), 0))
                    .playtimeForever(defaultInteger(gameSaveVO.getPlaytimeForever(), 0))
                    .playtimeTwoWeeks(defaultInteger(gameSaveVO.getPlaytimeTwoWeeks(), 0))
                    .lastPlayedTime(parseDateTime(gameSaveVO.getLastPlayedTime()))
                    .reviewContent(gameSaveVO.getReviewContent())
                    .build();
        }
        boolean synced = !SOURCE_CUSTOM.equals(original.getSource());
        original.setGameName(synced ? original.getGameName() : gameSaveVO.getGameName());
        original.setGameAlias(gameSaveVO.getGameAlias());
        original.setGameIntro(gameSaveVO.getGameIntro());
        original.setCustomCover(gameSaveVO.getCustomCover());
        original.setGameUrl(synced ? original.getGameUrl() : gameSaveVO.getGameUrl());
        original.setPlatforms(joinPlatforms(gameSaveVO.getPlatformList(), synced ? original.getSource() : null));
        original.setTags(joinTags(gameSaveVO.getTagList()));
        original.setScreenshots(joinScreenshots(resolveSubmittedScreenshotList(gameSaveVO)));
        original.setScreenshotLayout(resolveSubmittedScreenshotLayout(gameSaveVO));
        original.setPlayStatus(normalizePlayStatus(gameSaveVO.getPlayStatus()));
        original.setPersonalScore(gameSaveVO.getPersonalScore());
        original.setSortOrder(defaultInteger(gameSaveVO.getSortOrder(), 9999));
        original.setIsVisible(defaultInteger(gameSaveVO.getIsVisible(), 0));
        if (!synced) {
            original.setPlaytimeForever(defaultInteger(gameSaveVO.getPlaytimeForever(), 0));
            original.setPlaytimeTwoWeeks(defaultInteger(gameSaveVO.getPlaytimeTwoWeeks(), 0));
            original.setLastPlayedTime(parseDateTime(gameSaveVO.getLastPlayedTime()));
        }
        original.setReviewContent(gameSaveVO.getReviewContent());
        return original;
    }

    private void appendGameNameCondition(LambdaQueryWrapper<Game> wrapper, String keywords) {
        if (!StringUtils.hasText(keywords)) {
            return;
        }
        wrapper.and(item -> item.like(Game::getGameName, keywords)
                .or()
                .like(Game::getGameAlias, keywords));
    }

    private String resolveGameName(Game game) {
        return StringUtils.hasText(game.getGameAlias()) ? game.getGameAlias() : game.getGameName();
    }

    private void saveGameFields(Integer gameId, List<GameFieldVO> fieldVOList) {
        gameFieldService.remove(new LambdaQueryWrapper<GameField>().eq(GameField::getGameId, gameId));
        if (fieldVOList == null || fieldVOList.isEmpty()) {
            return;
        }
        List<GameField> fieldList = new ArrayList<>();
        for (int i = 0; i < fieldVOList.size(); i++) {
            GameFieldVO fieldVO = fieldVOList.get(i);
            if (Objects.isNull(fieldVO) || !StringUtils.hasText(fieldVO.getFieldName())) {
                continue;
            }
            String fieldType = normalizeFieldType(fieldVO.getFieldType());
            fieldList.add(GameField.builder()
                    .gameId(gameId)
                    .fieldName(fieldVO.getFieldName())
                    .fieldValue(fieldVO.getFieldValue())
                    .fieldUnit(fieldVO.getFieldUnit())
                    .fieldType(fieldType)
                    .groupName(defaultString(fieldVO.getGroupName(), "游戏数据"))
                    .showOnCard(defaultInteger(fieldVO.getShowOnCard(), 0))
                    .sortOrder(defaultInteger(fieldVO.getSortOrder(), i + 1))
                    .build());
        }
        if (!fieldList.isEmpty()) {
            gameFieldService.saveBatch(fieldList, 100);
        }
    }

    private void saveGameScreenshots(Integer gameId, GameSaveVO gameSaveVO) {
        gameScreenshotService.remove(new LambdaQueryWrapper<GameScreenshot>()
                .eq(GameScreenshot::getGameId, gameId));
        List<GameScreenshotSaveVO> screenshotVOList = gameSaveVO.getScreenshotItemList();
        if (screenshotVOList == null) {
            screenshotVOList = buildLegacyScreenshotSaveList(gameSaveVO.getScreenshotList());
        }
        if (screenshotVOList.isEmpty()) {
            return;
        }
        List<GameScreenshot> screenshotList = new ArrayList<>();
        for (int i = 0; i < screenshotVOList.size(); i++) {
            GameScreenshotSaveVO screenshotVO = screenshotVOList.get(i);
            if (screenshotVO == null || !StringUtils.hasText(screenshotVO.getDisplayUrl())) {
                continue;
            }
            String displayUrl = screenshotVO.getDisplayUrl().trim();
            screenshotList.add(GameScreenshot.builder()
                    .gameId(gameId)
                    .originalUrl(defaultString(screenshotVO.getOriginalUrl(), displayUrl).trim())
                    .displayUrl(displayUrl)
                    .originalWidth(positiveInteger(screenshotVO.getOriginalWidth()))
                    .originalHeight(positiveInteger(screenshotVO.getOriginalHeight()))
                    .displayWidth(positiveInteger(screenshotVO.getDisplayWidth()))
                    .displayHeight(positiveInteger(screenshotVO.getDisplayHeight()))
                    .frameType(normalizeScreenshotFrame(screenshotVO.getFrameType()))
                    .columnSpan(normalizeScreenshotColumnSpan(screenshotVO.getColumnSpan()))
                    .sortOrder(i + 1)
                    .build());
        }
        if (!screenshotList.isEmpty()) {
            gameScreenshotService.saveBatch(screenshotList, 100);
        }
    }

    private List<GameScreenshotSaveVO> buildLegacyScreenshotSaveList(List<String> screenshotList) {
        if (screenshotList == null || screenshotList.isEmpty()) {
            return new ArrayList<>();
        }
        List<GameScreenshotSaveVO> result = new ArrayList<>();
        for (int i = 0; i < screenshotList.size(); i++) {
            String url = screenshotList.get(i);
            if (!StringUtils.hasText(url)) {
                continue;
            }
            result.add(GameScreenshotSaveVO.builder()
                    .originalUrl(url.trim())
                    .displayUrl(url.trim())
                    .frameType("AUTO")
                    .columnSpan(6)
                    .sortOrder(i + 1)
                    .build());
        }
        return result;
    }

    private List<GameScreenshotDTO> convertGameScreenshots(List<GameScreenshot> screenshotList,
                                                           List<String> legacyScreenshotList,
                                                           boolean admin) {
        List<GameScreenshot> sourceList = screenshotList;
        if (sourceList == null || sourceList.isEmpty()) {
            sourceList = buildLegacyGameScreenshots(legacyScreenshotList);
        }
        return sourceList.stream()
                .map(item -> convertGameScreenshot(item, admin))
                .collect(Collectors.toList());
    }

    private GameScreenshotDTO convertGameScreenshot(GameScreenshot screenshot, boolean admin) {
        GameScreenshotDTO screenshotDTO = admin ? new GameScreenshotBackDTO() : new GameScreenshotDTO();
        screenshotDTO.setDisplayUrl(screenshot.getDisplayUrl());
        screenshotDTO.setDisplayWidth(screenshot.getDisplayWidth());
        screenshotDTO.setDisplayHeight(screenshot.getDisplayHeight());
        screenshotDTO.setFrameType(normalizeScreenshotFrame(screenshot.getFrameType()));
        screenshotDTO.setColumnSpan(normalizeScreenshotColumnSpan(screenshot.getColumnSpan()));
        screenshotDTO.setSortOrder(screenshot.getSortOrder());
        if (admin) {
            GameScreenshotBackDTO backDTO = (GameScreenshotBackDTO) screenshotDTO;
            backDTO.setOriginalUrl(defaultString(screenshot.getOriginalUrl(), screenshot.getDisplayUrl()));
            backDTO.setOriginalWidth(screenshot.getOriginalWidth());
            backDTO.setOriginalHeight(screenshot.getOriginalHeight());
        }
        return screenshotDTO;
    }

    private List<GameScreenshot> buildLegacyGameScreenshots(List<String> legacyScreenshotList) {
        List<GameScreenshot> result = new ArrayList<>();
        for (int i = 0; i < legacyScreenshotList.size(); i++) {
            String url = legacyScreenshotList.get(i);
            result.add(GameScreenshot.builder()
                    .originalUrl(url)
                    .displayUrl(url)
                    .frameType("AUTO")
                    .columnSpan(6)
                    .sortOrder(i + 1)
                    .build());
        }
        return result;
    }

    private List<String> resolveDisplayScreenshotList(List<GameScreenshot> screenshotList,
                                                      List<String> legacyScreenshotList) {
        if (screenshotList == null || screenshotList.isEmpty()) {
            return legacyScreenshotList;
        }
        return screenshotList.stream()
                .map(GameScreenshot::getDisplayUrl)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    private JSONArray fetchSteamGames(boolean includeFreeGames, String steamApiKey, String steamId) {
        String response = HttpRequest.get(STEAM_OWNED_GAMES_URL)
                .header("x-webapi-key", steamApiKey)
                .form("steamid", steamId)
                .form("include_appinfo", true)
                .form("include_played_free_games", includeFreeGames)
                .form("format", "json")
                .timeout(15000)
                .execute()
                .body();
        JSONObject result = JSON.parseObject(response);
        if (Objects.isNull(result) || Objects.isNull(result.getJSONObject("response"))) {
            throw new BizException("Steam游戏库接口返回异常");
        }
        return result.getJSONObject("response").getJSONArray("games");
    }

    private Game buildSteamGame(JSONObject item, LocalDateTime syncTime) {
        String appId = item.getString("appid");
        return Game.builder()
                .source(SOURCE_STEAM)
                .sourceGameId(appId)
                .gameName(item.getString("name"))
                .sourceCover(buildSteamCover(appId))
                .gameUrl("https://store.steampowered.com/app/" + appId)
                .platforms(SOURCE_STEAM)
                .playStatus(DEFAULT_STATUS)
                .sortOrder(9999)
                .isVisible(0)
                .playtimeForever(defaultInteger(item.getInteger("playtime_forever"), 0))
                .playtimeTwoWeeks(defaultInteger(item.getInteger("playtime_2weeks"), 0))
                .lastPlayedTime(parseSteamTime(item.getLong("rtime_last_played")))
                .syncTime(syncTime)
                .build();
    }

    private void refreshSteamGame(Game game, JSONObject item, LocalDateTime syncTime) {
        game.setGameName(item.getString("name"));
        game.setSourceCover(buildSteamCover(game.getSourceGameId()));
        game.setGameUrl("https://store.steampowered.com/app/" + game.getSourceGameId());
        game.setPlaytimeForever(defaultInteger(item.getInteger("playtime_forever"), 0));
        game.setPlaytimeTwoWeeks(defaultInteger(item.getInteger("playtime_2weeks"), 0));
        game.setLastPlayedTime(parseSteamTime(item.getLong("rtime_last_played")));
        game.setSyncTime(syncTime);
    }

    private String buildSteamCover(String appId) {
        return "https://shared.cloudflare.steamstatic.com/store_item_assets/steam/apps/"
                + appId + "/header.jpg";
    }

    private LocalDateTime parseSteamTime(Long timestamp) {
        if (Objects.isNull(timestamp) || timestamp <= 0) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), SHANGHAI_ZONE);
    }

    private LocalDateTime parseDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDateTime.parse(value);
            } catch (DateTimeParseException e) {
                log.warn("游戏时间字段解析失败：{}", value);
                return null;
            }
        }
    }

    private String resolveCover(Game game) {
        return StringUtils.hasText(game.getCustomCover()) ? game.getCustomCover() : game.getSourceCover();
    }

    private String joinPlatforms(List<String> platformList, String requiredPlatform) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        if (StringUtils.hasText(requiredPlatform)) {
            result.add(normalizePlatform(requiredPlatform));
        }
        if (platformList != null) {
            platformList.stream()
                    .map(this::normalizePlatform)
                    .filter(PLATFORM_SET::contains)
                    .forEach(result::add);
        }
        if (result.isEmpty()) {
            result.add("PC");
        }
        return String.join(",", result);
    }

    private List<String> splitPlatforms(String platforms) {
        if (!StringUtils.hasText(platforms)) {
            return new ArrayList<>();
        }
        return Arrays.stream(platforms.split(","))
                .map(this::normalizePlatform)
                .filter(PLATFORM_SET::contains)
                .distinct()
                .collect(Collectors.toList());
    }

    private String joinTags(List<String> tagList) {
        if (tagList == null || tagList.isEmpty()) {
            return null;
        }
        LinkedHashSet<String> result = tagList.stream()
                .filter(StringUtils::hasText)
                .flatMap(item -> Arrays.stream(item.split("[,，]")))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return result.isEmpty() ? null : String.join(",", result);
    }

    private List<String> splitTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return new ArrayList<>();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> resolveSubmittedScreenshotList(GameSaveVO gameSaveVO) {
        if (gameSaveVO.getScreenshotItemList() == null) {
            return gameSaveVO.getScreenshotList();
        }
        return gameSaveVO.getScreenshotItemList().stream()
                .filter(Objects::nonNull)
                .map(GameScreenshotSaveVO::getDisplayUrl)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    private String resolveSubmittedScreenshotLayout(GameSaveVO gameSaveVO) {
        return gameSaveVO.getScreenshotItemList() == null
                ? normalizeScreenshotLayout(gameSaveVO.getScreenshotLayout()) : "CUSTOM";
    }

    private String joinScreenshots(List<String> screenshotList) {
        if (screenshotList == null || screenshotList.isEmpty()) {
            return null;
        }
        List<String> result = screenshotList.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        return result.isEmpty() ? null : JSON.toJSONString(result);
    }

    private List<String> splitScreenshots(String screenshots) {
        if (!StringUtils.hasText(screenshots)) {
            return new ArrayList<>();
        }
        try {
            return JSON.parseArray(screenshots, String.class).stream()
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("游戏截图字段解析失败");
            return new ArrayList<>();
        }
    }

    private String normalizeScreenshotLayout(String screenshotLayout) {
        String layout = StringUtils.hasText(screenshotLayout)
                ? screenshotLayout.trim().toUpperCase() : "CAROUSEL";
        return SCREENSHOT_LAYOUT_SET.contains(layout) ? layout : "CAROUSEL";
    }

    private String normalizeScreenshotFrame(String frameType) {
        String frame = StringUtils.hasText(frameType) ? frameType.trim().toUpperCase() : "AUTO";
        return SCREENSHOT_FRAME_SET.contains(frame) ? frame : "AUTO";
    }

    private Integer normalizeScreenshotColumnSpan(Integer columnSpan) {
        return SCREENSHOT_COLUMN_SPAN_SET.contains(columnSpan) ? columnSpan : 6;
    }

    private String normalizePlatform(String platform) {
        if (!StringUtils.hasText(platform)) {
            return "";
        }
        String value = platform.trim().toUpperCase();
        return Arrays.asList("WEGAME", "BATTLE_NET", "XBOX", "OTHER").contains(value)
                ? "PC" : value;
    }

    private String normalizePlayStatus(String playStatus) {
        if (!StringUtils.hasText(playStatus)) {
            return DEFAULT_STATUS;
        }
        String status = playStatus.trim().toUpperCase();
        if ("COMPLETED".equals(status) || "PAUSED".equals(status)) {
            return PLAYED_STATUS;
        }
        return PLAY_STATUS_SET.contains(status) ? status : DEFAULT_STATUS;
    }

    private void appendPlayStatusCondition(LambdaQueryWrapper<Game> wrapper, String playStatus) {
        if (!StringUtils.hasText(playStatus)) {
            return;
        }
        String status = normalizePlayStatus(playStatus);
        if (PLAYED_STATUS.equals(status)) {
            wrapper.in(Game::getPlayStatus, PLAYED_STATUS, "COMPLETED", "PAUSED");
        } else {
            wrapper.eq(Game::getPlayStatus, status);
        }
    }

    private String normalizeFieldType(String fieldType) {
        String type = StringUtils.hasText(fieldType) ? fieldType.trim().toUpperCase() : "TEXT";
        return FIELD_TYPE_SET.contains(type) ? type : "TEXT";
    }

    private long normalizePage(Long value, long defaultValue) {
        return Objects.isNull(value) || value < 1 ? defaultValue : value;
    }

    private Integer defaultInteger(Integer value, Integer defaultValue) {
        return Objects.isNull(value) ? defaultValue : value;
    }

    private Integer positiveInteger(Integer value) {
        return Objects.isNull(value) || value <= 0 ? null : value;
    }

    private String defaultString(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
