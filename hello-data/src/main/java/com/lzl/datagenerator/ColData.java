package com.lzl.datagenerator;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
public interface ColData {
    String getName();
    Object getDefaultVal();
    Object getNextVal();
    void reset();
}