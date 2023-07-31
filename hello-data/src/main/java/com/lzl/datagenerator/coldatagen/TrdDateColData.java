package com.lzl.datagenerator.coldatagen;

import cn.hutool.db.DbUtil;

import java.sql.SQLException;

/**
 * 版权声明：本程序模块属于后台业务系统（FSPT）的一部分
 * 金证科技股份有限公司 版权所有<br>
 * <p>
 * 模块名称：期权业务-<br>
 * 模块描述：期权业务-<br>
 * 开发作者：李正良<br>
 * 创建日期：2023/07/28<br>
 * 模块版本：1.0.0.0<br>
 * ----------------------------------------------------------------<br>
 * 修改日期      版本       作者      备注<br>
 * 2023/07/28   1.0.0.0   李正良      创建<br>
 * -----------------------------------------------------------------</p>
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