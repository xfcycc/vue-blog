package com.minzheng.blog.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * spring工具类
 * 用于获取spring容器中的bean
 *
 * @author caiguoyu
 * @date 2024/1/27
 */
@Component
public class ApplicationContextUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    /**
     * 获取spring容器中的bean
     * 自动注入
     */
    public static <T> T autowire(T bean) {
        applicationContext.getAutowireCapableBeanFactory().autowireBean(bean);
        return bean;
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        ApplicationContextUtils.applicationContext = applicationContext;
    }

    /**
     * 手动获取spring容器中的bean
     */

    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }
}