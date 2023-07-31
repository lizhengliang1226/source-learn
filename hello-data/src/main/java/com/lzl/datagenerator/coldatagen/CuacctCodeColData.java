package com.lzl.datagenerator.coldatagen;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
public class CuacctCodeColData extends BaseColData{
    private Long baseVal = 2200000000L;

    @Override
    public String getName() {
        return "CUACCT_CODE";
    }

    @Override
    public Object getNextVal() {
        return baseVal++;
    }

    @Override
    public void reset() {
        baseVal=2200000000L;
    }
}