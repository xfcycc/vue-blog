package com.minzheng.blog.util;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

/**
 * @description: 处理自调用失效的事务工具类<br>
 * 调用此工具类的方法不必加@{@link Transactional}注解<br>
 * 1.可以作用于私有方法
 * 2.可以在不同的事务传播中调用相同的方法
 * @date: 2024/2/5 15:51
 * @author KongCong
 * @version 1.0
 */
@Component
public class TransactionRunner {

    /**
     * @description: 用事务包装需要执行的操作
     * @author: KongCong
     * @date: 2024/2/5 15:51
     * @param supplier 需要执行的操作
     * @return T
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class}, propagation = Propagation.REQUIRED)
    public <T> T run(Supplier<T> supplier) {
        return supplier.get();
    }

    /**
     * @description: 用事务包装需要执行的操作
     * @author: KongCong
     * @date: 2024/2/5 15:53
     * @param runnable  需要执行的操作
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class}, propagation = Propagation.REQUIRED)
    public void run(Runnable runnable) {
        runnable.run();
    }

    /**
     * @description: 使用新的事务来执行操作
     * @author: KongCong
     * @date: 2024/2/5 15:51
     * @param supplier 需要执行的操作
     * @return T
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class}, propagation = Propagation.REQUIRES_NEW)
    public <T> T runNew(Supplier<T> supplier) {
        return supplier.get();
    }
}
