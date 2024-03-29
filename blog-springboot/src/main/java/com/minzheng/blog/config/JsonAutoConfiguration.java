package com.minzheng.blog.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.minzheng.blog.convert.StringToLocalDateConverter;
import com.minzheng.blog.convert.StringToLocalDateTimeConverter;
import com.minzheng.blog.convert.StringToLocalTimeConverter;
import com.minzheng.blog.serialization.BigNumberSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * @author KongCong
 * @version 1.0
 * @description: json序列化自动配置
 * @date: 2024/2/28 18:54
 */
@Configuration
public class JsonAutoConfiguration {

    private static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> {
            // 配置 Jackson 序列化 BigDecimal 时使用的格式
            builder.serializerByType(BigDecimal.class, ToStringSerializer.instance);

            // 配置 Jackson 序列化 long类型为String，解决后端返回的Long类型在前端精度丢失的问题
            builder.serializerByType(BigInteger.class, BigNumberSerializer.SINGLETON);
            builder.serializerByType(Long.class, BigNumberSerializer.SINGLETON);
            builder.serializerByType(Long.TYPE, BigNumberSerializer.SINGLETON);

            // 配置 Jackson 序列化 LocalDateTime、LocalDate、LocalTime 时使用的格式
            builder.serializers(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATETIME_PATTERN)));
            builder.serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)));
            builder.serializers(new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));


            // 配置 Jackson 反序列化 LocalDateTime、LocalDate、LocalTime 时使用的格式
            builder.deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATETIME_PATTERN)));
            builder.deserializerByType(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)));
            builder.deserializerByType(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));
        };
    }

    @Bean
    @ConditionalOnMissingBean(StringToLocalDateTimeConverter.class)
    public StringToLocalDateTimeConverter stringToLocalDateTimeConverter(
            @Value("${blog.date-time-format:}") String pattern) {
        return new StringToLocalDateTimeConverter(StringUtils.hasText(pattern) ? pattern :
                DEFAULT_DATETIME_PATTERN);
    }

    @Bean
    @ConditionalOnMissingBean(StringToLocalDateConverter.class)
    public StringToLocalDateConverter stringToLocalDateConverter(
            @Value("${blog.date-format:}") String pattern) {
        return new StringToLocalDateConverter(StringUtils.hasText(pattern) ? pattern :
                DEFAULT_DATE_FORMAT);
    }

    @Bean
    @ConditionalOnMissingBean(StringToLocalTimeConverter.class)
    public StringToLocalTimeConverter stringToLocalTimeConverter(
            @Value("${blog.time-format:}") String pattern) {
        return new StringToLocalTimeConverter(StringUtils.hasText(pattern) ? pattern :
                DEFAULT_TIME_FORMAT);
    }
}

