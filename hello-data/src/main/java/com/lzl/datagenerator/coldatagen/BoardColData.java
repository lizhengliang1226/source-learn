package com.lzl.datagenerator.coldatagen;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
public class BoardColData extends BaseColData {

    @Override
    public String getName() {
        return "BOARD";
    }

    private String board = "15";

    @Override
    public Object getNextVal() {
        return board;
    }

    @Override
    public void reset() {

    }
}