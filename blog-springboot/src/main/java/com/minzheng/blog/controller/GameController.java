package com.minzheng.blog.controller;

import com.minzheng.blog.annotation.OptLog;
import com.minzheng.blog.dto.GameBackDTO;
import com.minzheng.blog.dto.GameConfigDTO;
import com.minzheng.blog.dto.GameDeleteDTO;
import com.minzheng.blog.dto.GameDetailDTO;
import com.minzheng.blog.dto.GameImageSourceDTO;
import com.minzheng.blog.dto.GameListDTO;
import com.minzheng.blog.dto.GameOneTimeSyncDTO;
import com.minzheng.blog.dto.SteamSyncDTO;
import com.minzheng.blog.enums.FilePathEnum;
import com.minzheng.blog.service.GameConfigService;
import com.minzheng.blog.service.GameImageService;
import com.minzheng.blog.service.GameOneTimeSyncService;
import com.minzheng.blog.service.GameService;
import com.minzheng.blog.strategy.context.UploadStrategyContext;
import com.minzheng.blog.vo.GameBackQueryVO;
import com.minzheng.blog.vo.GameConfigSaveVO;
import com.minzheng.blog.vo.GameDeleteVO;
import com.minzheng.blog.vo.GameIdVO;
import com.minzheng.blog.vo.GameImageSourceVO;
import com.minzheng.blog.vo.GameQueryVO;
import com.minzheng.blog.vo.GameSaveVO;
import com.minzheng.blog.vo.PageResult;
import com.minzheng.blog.vo.PsnOneTimeSyncVO;
import com.minzheng.blog.vo.Result;
import com.minzheng.blog.vo.SteamSyncVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

import java.util.List;

import static com.minzheng.blog.constant.OptTypeConst.REMOVE;
import static com.minzheng.blog.constant.OptTypeConst.SAVE_OR_UPDATE;

/**
 * 游戏档案控制器
 *
 * @author caiguoyu
 * @date 2026/07/17
 */
@Api(tags = "游戏档案模块")
@RestController
public class GameController {

    @Resource
    private GameService gameService;

    @Resource
    private GameConfigService gameConfigService;

    @Resource
    private GameOneTimeSyncService gameOneTimeSyncService;

    @Resource
    private GameImageService gameImageService;

    @Resource
    private UploadStrategyContext uploadStrategyContext;

    @ApiOperation(value = "查看公开游戏列表")
    @PostMapping("/games/list")
    public Result<GameListDTO> listGames(@RequestBody(required = false) GameQueryVO queryVO) {
        return Result.ok(gameService.listGames(queryVO));
    }

    @ApiOperation(value = "查看公开游戏详情")
    @PostMapping("/games/detail")
    public Result<GameDetailDTO> getGameDetail(@RequestBody GameIdVO gameIdVO) {
        return Result.ok(gameService.getGameDetail(gameIdVO, false));
    }

    @ApiOperation(value = "查看后台游戏列表")
    @PostMapping("/admin/games/list")
    public Result<PageResult<GameBackDTO>> listBackGames(@RequestBody(required = false) GameBackQueryVO queryVO) {
        return Result.ok(gameService.listBackGames(queryVO));
    }

    @ApiOperation(value = "查看后台游戏详情")
    @PostMapping("/admin/games/detail")
    public Result<GameDetailDTO> getBackGameDetail(@RequestBody GameIdVO gameIdVO) {
        return Result.ok(gameService.getGameDetail(gameIdVO, true));
    }

    @OptLog(optType = SAVE_OR_UPDATE)
    @ApiOperation(value = "保存或修改游戏")
    @PostMapping("/admin/games/save")
    public Result<Integer> saveOrUpdateGame(@RequestBody GameSaveVO gameSaveVO) {
        return Result.ok(gameService.saveOrUpdateGame(gameSaveVO));
    }

    @OptLog(optType = REMOVE)
    @ApiOperation(value = "删除自定义游戏")
    @PostMapping("/admin/games/delete")
    public Result<GameDeleteDTO> deleteGames(@RequestBody GameDeleteVO deleteVO) {
        return Result.ok(gameService.deleteGames(deleteVO));
    }

    @ApiOperation(value = "查看游戏平台同步配置")
    @PostMapping("/admin/games/config/list")
    public Result<List<GameConfigDTO>> listGameConfigs() {
        return Result.ok(gameConfigService.listGameConfigs());
    }

    @ApiOperation(value = "保存游戏平台同步配置")
    @PostMapping("/admin/games/config/save")
    public Result<?> saveGameConfigs(@RequestBody GameConfigSaveVO saveVO) {
        gameConfigService.saveGameConfigs(saveVO);
        return Result.ok();
    }

    @ApiOperation(value = "手动同步Steam游戏")
    @PostMapping("/admin/games/steam/sync")
    public Result<SteamSyncDTO> syncSteamGames(@RequestBody(required = false) SteamSyncVO syncVO) {
        return Result.ok(gameService.syncSteamGames(syncVO));
    }

    @ApiOperation(value = "一次性同步PSN游戏")
    @PostMapping("/admin/games/psn/sync-once")
    public Result<GameOneTimeSyncDTO> syncPsnGames(@RequestBody PsnOneTimeSyncVO syncVO) {
        return Result.ok(gameOneTimeSyncService.syncPsnGames(syncVO));
    }

    @ApiOperation(value = "上传游戏图片")
    @ApiImplicitParam(name = "file", value = "游戏图片", required = true, dataType = "MultipartFile")
    @PostMapping("/admin/games/images")
    public Result<String> saveGameImage(MultipartFile file) {
        return Result.ok(uploadStrategyContext.executeUploadStrategy(file, FilePathEnum.GAME.getPath()));
    }

    @ApiOperation(value = "读取游戏图片源文件")
    @PostMapping("/admin/games/images/source")
    public Result<GameImageSourceDTO> getGameImageSource(@RequestBody GameImageSourceVO sourceVO) {
        return Result.ok(gameImageService.getImageSource(sourceVO));
    }
}
