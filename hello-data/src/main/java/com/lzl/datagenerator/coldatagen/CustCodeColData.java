package com.lzl.datagenerator.coldatagen;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
public class CustCodeColData extends BaseColData {
    private  Long baseVal = 1200000000L;

    @Override
    public String getName() {
        return "CUST_CODE";
    }


    @Override
    public Object getNextVal() {
        return baseVal++;
    }

    @Override
    public void reset() {
        baseVal = 1200000000L;
    }
}