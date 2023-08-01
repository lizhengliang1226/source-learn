package com.lzl.datagenerator.coldatagen;

import cn.hutool.db.DbUtil;

import java.sql.SQLException;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
public class TrdDateColData extends BaseColData {


    @Override
    public String getName() {
        return "TRD_DATE";
    }

    private Number trdDate;

    @Override
    public Object getNextVal() {
        if (trdDate != null) {
            return trdDate;
        }
        try {
            trdDate = DbUtil.use().queryNumber(" SELECT MIN(TRD_DATE) TRD_DATE FROM BOARD WHERE BOARD_STATUS = '0'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return trdDate.intValue();
    }

    @Override
    public void reset() {

    }
}