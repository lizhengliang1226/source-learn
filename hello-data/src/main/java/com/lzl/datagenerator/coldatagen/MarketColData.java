package com.lzl.datagenerator.coldatagen;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
public class MarketColData extends BaseColData {


    @Override
    public String getName() {
        return "MARKET";
    }

    private String market ="1";

    @Override
    public Object getNextVal() {
       return market;
    }

    @Override
    public void reset() {

    }
}