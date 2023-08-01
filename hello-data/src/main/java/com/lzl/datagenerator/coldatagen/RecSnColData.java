package com.lzl.datagenerator.coldatagen;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
public class RecSnColData extends BaseColData {

    @Override
    public String getName() {
        return "REC_SN";
    }

    @Override
    public void reset() {
        baseVal=0L;
    }
}