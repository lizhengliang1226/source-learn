package com.lzl.datagenerator.coldatagen;

import cn.hutool.db.DbUtil;

import java.sql.SQLException;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
public class SettDateColData extends BaseColData {


    @Override
    public String getName() {
        return "SETT_DATE";
    }

    private Number settDate;

    @Override
    public Object getNextVal() {
        if (settDate != null) {
            return settDate;
        }
        try {
            settDate = DbUtil.use().queryNumber(" SELECT MIN (SETT_DATE) SETT_DATE FROM BOARD WHERE BOARD_STATUS = '0'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return settDate.intValue();
    }

    @Override
    public void reset() {

    }
}