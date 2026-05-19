package com.minzheng.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 博客启动类
 *
 * @author yezhiqiu
 * @date 2021/08/14
 */
@MapperScan("com.minzheng.blog.dao")
@SpringBootApplication
@EnableScheduling
public class BlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }

}
