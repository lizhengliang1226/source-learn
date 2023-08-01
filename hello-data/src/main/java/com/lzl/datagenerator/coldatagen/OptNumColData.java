package com.lzl.datagenerator.coldatagen;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
public class OptNumColData extends BaseColData{
    private Long baseVal = 54000000L;

    @Override
    public String getName() {
        return "OPT_NUM";
    }

    @Override
    public Object getNextVal() {
        return baseVal++;
    }

    @Override
    public void reset() {
        baseVal = 54000000L;
    }
}