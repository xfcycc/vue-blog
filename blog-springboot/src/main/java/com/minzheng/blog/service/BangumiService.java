package com.minzheng.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.minzheng.blog.dto.BangumiDTO;
import com.minzheng.blog.entity.Bangumi;

import java.util.List;

/**
 * B站追番服务
 *
 * @author caiguoyu
 * @date 2026/06/09
 */
public interface BangumiService extends IService<Bangumi> {

    /**
     * 查询追番列表
     *
     * @return 追番列表
     */
    List<BangumiDTO> listBangumis();

    /**
     * 同步B站追番列表
     */
    void syncBangumis();

}
