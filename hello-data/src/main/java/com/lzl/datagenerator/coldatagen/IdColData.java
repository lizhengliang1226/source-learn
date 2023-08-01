package com.lzl.datagenerator.coldatagen;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
public class IdColData extends BaseColData {


    @Override
    public String getName() {
        return "id";
    }

    @Override
    public void reset() {
        baseVal=0L;
    }


}