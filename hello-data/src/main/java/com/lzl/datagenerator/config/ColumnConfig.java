package com.lzl.datagenerator.config;

import java.util.List;

/**
 * 版权声明：本程序模块属于后台业务系统（FSPT）的一部分
 * 金证科技股份有限公司 版权所有<br>
 * <p>
 * 模块名称：期权业务-<br>
 * 模块描述：期权业务-<br>
 * 开发作者：李正良<br>
 * 创建日期：2023/08/01<br>
 * 模块版本：1.0.0.0<br>
 * ----------------------------------------------------------------<br>
 * 修改日期      版本       作者      备注<br>
 * 2023/08/01   1.0.0.0   李正良      创建<br>
 * -----------------------------------------------------------------</p>
 */
public class ColumnConfig {
    private String colName;
    private String strategy;
    private Object fixedValue;
    private Number baseValue;
    private List<Object> randomEle;
    private String querySql;
    private String queryCol;

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public Object getFixedValue() {
        return fixedValue;
    }

    public void setFixedValue(Object fixedValue) {
        this.fixedValue = fixedValue;
    }

    public Number getBaseValue() {
        return baseValue;
    }

    public void setBaseValue(Number baseValue) {
        this.baseValue = baseValue;
    }

    public List<Object> getRandomEle() {
        return randomEle;
    }

    public void setRandomEle(List<Object> randomEle) {
        this.randomEle = randomEle;
    }

    public String getQuerySql() {
        return querySql;
    }

    public void setQuerySql(String querySql) {
        this.querySql = querySql;
    }

    public String getQueryCol() {
        return queryCol;
    }

    public void setQueryCol(String queryCol) {
        this.queryCol = queryCol;
    }

    @Override
    public String toString() {
        return "ColumnConfig{" +
                "colName='" + colName + '\'' +
                ", strategy='" + strategy + '\'' +
                ", fixedValue=" + fixedValue +
                ", baseValue=" + baseValue +
                ", randomEle=" + randomEle +
                ", querySql='" + querySql + '\'' +
                ", queryCol='" + queryCol + '\'' +
                '}';
    }
}