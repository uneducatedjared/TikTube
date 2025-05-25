package com.buguagaoshu.tiktube.repository;

/**
 * 登录和邮件发送次数限制接口
 * 用于控制用户登录尝试次数和邮件发送频率
 * 
 */
public interface CountLimitRepository {
    /**
     * 检查是否允许用户登录
     * 根据用户邮箱判断是否超过登录失败次数限制
     *
     * @param key 用户邮箱地址
     * @return true 表示允许登录，false 表示超过限制次数
     */
    boolean allowLogin(String key);

    /**
     * 记录一次登录失败尝试
     * 增加用户的登录失败次数计数
     *
     * @param email 用户邮箱地址
     */
    void recordFailedAttempt(String email);

    /**
     * 记录一次登录成功
     * 重置用户的登录失败次数计数
     *
     * @param email 用户邮箱地址
     */
    void recordSuccess(String email);

    /**
     * 获取用户剩余的登录尝试次数
     *
     * @param email 用户邮箱地址
     * @return 剩余可尝试次数
     */
    int getRemainingAttempts(String email);
    
    /**
     * 检查是否允许发送邮件
     * 根据邮箱地址或IP地址判断是否超过发送频率限制
     *
     * @param key 邮箱地址或IP地址
     * @return true 表示允许发送，false 表示超过限制
     */
    boolean allowSendEmail(String key);
    
    /**
     * 记录一次邮件发送
     * 增加邮件发送次数计数
     *
     * @param key 邮箱地址或IP地址
     */
    void recordEmailSent(String key);
    
    /**
     * 获取剩余邮件发送次数
     * 返回指定邮箱或IP地址的剩余发送次数
     *
     * @param key 邮箱地址或IP地址
     * @return 剩余可发送次数
     */
    int getRemainingEmailSendAttempts(String key);
}
