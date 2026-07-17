package com.minzheng.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.minzheng.blog.dto.GameBackDTO;
import com.minzheng.blog.dto.GameDeleteDTO;
import com.minzheng.blog.dto.GameDetailDTO;
import com.minzheng.blog.dto.GameListDTO;
import com.minzheng.blog.dto.SteamSyncDTO;
import com.minzheng.blog.entity.Game;
import com.minzheng.blog.vo.GameBackQueryVO;
import com.minzheng.blog.vo.GameDeleteVO;
import com.minzheng.blog.vo.GameIdVO;
import com.minzheng.blog.vo.GameQueryVO;
import com.minzheng.blog.vo.GameSaveVO;
import com.minzheng.blog.vo.PageResult;
import com.minzheng.blog.vo.SteamSyncVO;

/**
 * 游戏档案服务
 */
public interface GameService extends IService<Game> {

    GameListDTO listGames(GameQueryVO queryVO);

    GameDetailDTO getGameDetail(GameIdVO gameIdVO, boolean admin);

    PageResult<GameBackDTO> listBackGames(GameBackQueryVO queryVO);

    Integer saveOrUpdateGame(GameSaveVO gameSaveVO);

    GameDeleteDTO deleteGames(GameDeleteVO deleteVO);

    SteamSyncDTO syncSteamGames(SteamSyncVO syncVO);
}
