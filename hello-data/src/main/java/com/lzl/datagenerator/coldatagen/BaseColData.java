package com.lzl.datagenerator.coldatagen;


import com.lzl.datagenerator.ColData;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
public abstract class BaseColData implements ColData {
    protected Long baseVal=0L;
    @Override
    public Object getDefaultVal() {
        return 0;
    }

    @Override
    public Object getNextVal() {
        return baseVal++;
    }
}