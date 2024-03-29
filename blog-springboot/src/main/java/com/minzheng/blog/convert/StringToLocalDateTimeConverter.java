package com.minzheng.blog.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author KongCong
 * @version 1.0
 * @description: {@link String}类型的日期参数转换为 {@link LocalDateTime}
 * @date: 2024/3/15 16:46
 */
public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {

    private final DateTimeFormatter formatter;

    public StringToLocalDateTimeConverter(String pattern) {
        if (StringUtils.hasText(pattern)) {
            this.formatter = DateTimeFormatter.ofPattern(pattern);
        } else {
            this.formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        }

    }

    @Override
    public LocalDateTime convert(String source) {
        return !StringUtils.hasText(source) ? null : LocalDateTime.parse(source, this.formatter);
    }

}
