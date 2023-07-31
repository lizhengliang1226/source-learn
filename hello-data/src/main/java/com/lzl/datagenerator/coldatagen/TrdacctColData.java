package com.lzl.datagenerator.coldatagen;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
public class TrdacctColData extends BaseColData{
    private Long baseVal = 6200000L;

    @Override
    public String getName() {
        return "TRDACCT";
    }

    @Override
    public Object getNextVal() {
        return "A"+baseVal++;
    }

    @Override
    public void reset() {
        baseVal=6200000L;
    }
}