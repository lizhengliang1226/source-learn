package com.lzl.datagenerator.coldatagen;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
public class CurrencyColData extends BaseColData {


    @Override
    public String getName() {
        return "CURRENCY";
    }

    private String currency ="0";

    @Override
    public Object getNextVal() {
       return currency;
    }

    @Override
    public void reset() {

    }
}