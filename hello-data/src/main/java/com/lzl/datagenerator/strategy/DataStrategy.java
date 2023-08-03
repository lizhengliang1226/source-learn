package com.lzl.datagenerator.strategy;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
public interface DataStrategy {
    /**
     * 下一个值
     *
     * @return 下一个值
     */
    Object getNextVal();

    /**
     * 策略名称
     *
     * @return 策略名称
     */
    String getName();
}