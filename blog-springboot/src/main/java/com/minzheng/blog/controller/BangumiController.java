package com.minzheng.blog.controller;

import com.minzheng.blog.dto.BangumiDTO;
import com.minzheng.blog.service.BangumiService;
import com.minzheng.blog.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * B站追番控制器
 *
 * @author caiguoyu
 * @date 2026/06/09
 */
@Api(tags = "B站追番模块")
@RestController
public class BangumiController {

    @Resource
    private BangumiService bangumiService;

    /**
     * 查看追番列表
     *
     * @return 追番列表
     */
    @ApiOperation(value = "查看追番列表")
    @GetMapping("/bangumis")
    public Result<List<BangumiDTO>> listBangumis() {
        return Result.ok(bangumiService.listBangumis());
    }

}
