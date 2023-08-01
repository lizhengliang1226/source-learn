package com.lzl.datagenerator.coldatagen;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
public class OrderIdColData extends BaseColData {
    private  Long baseVal = 5100000000L;

    @Override
    public String getName() {
        return "ORDER_ID";
    }


    @Override
    public Object getNextVal() {
        return baseVal++;
    }

    @Override
    public void reset() {
        baseVal = 5100000000L;
    }
}