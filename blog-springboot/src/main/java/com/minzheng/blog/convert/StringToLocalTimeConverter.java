package com.minzheng.blog.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * @author KongCong
 * @version 1.0
 * @description: {@link String}类型的日期参数转换为 {@link LocalTime}
 * @date: 2024/3/15 16:51
 */
public class StringToLocalTimeConverter implements Converter<String, LocalTime> {
    private final DateTimeFormatter formatter;

    public StringToLocalTimeConverter(String pattern) {
        if (StringUtils.hasText(pattern)) {
            this.formatter = DateTimeFormatter.ofPattern(pattern);
        } else {
            this.formatter = DateTimeFormatter.ISO_LOCAL_TIME;
        }

    }

    @Override
    public LocalTime convert(String source) {
        return !StringUtils.hasText(source) ? null : LocalTime.parse(source, this.formatter);
    }
}
