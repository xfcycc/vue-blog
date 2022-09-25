package com.minzheng.blog.controller;

import com.minzheng.blog.service.SiteMapService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * 站点地图控制器
 *
 * @author caiguoyu
 * @date 2022/9/25
 */
@Controller
public class SiteMapController {
    @Resource
    private SiteMapService siteMapService;

    @GetMapping("/sitemap.xml")
    public void siteMapXml(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType(MediaType.APPLICATION_XML_VALUE);
        try (Writer writer = response.getWriter()) {
            writer.append(siteMapService.createArticleMap());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
