package com.minzheng.blog.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author KongCong
 * @version 1.0
 * @description: {@link String}类型的日期参数转换为 {@link LocalDate}
 * @date: 2024/3/15 16:50
 */
public class StringToLocalDateConverter implements Converter<String, LocalDate> {
    private final DateTimeFormatter formatter;

    public StringToLocalDateConverter(String pattern) {
        if (StringUtils.hasText(pattern)) {
            this.formatter = DateTimeFormatter.ofPattern(pattern);
        } else {
            this.formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        }

    }

    @Override
    public LocalDate convert(String source) {
        return !StringUtils.hasText(source) ? null : LocalDate.parse(source, this.formatter);
    }
}
