package com.lzl.datagenerator.strategy;

import com.lzl.datagenerator.config.ColumnConfig;
import lombok.ToString;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
@ToString
public class AutoIncDataStrategy implements DataStrategy {
    private Number baseVal;
    private final Number originVal;
    @Override
    public Object getNextVal() {
        Number returnVal=baseVal;
        baseVal=baseVal.longValue()+1;
        return returnVal;
    }

    @Override
    public void reset() {
        baseVal=originVal;
    }

    @Override
    public String getName() {
        return "auto-inc";
    }

    public AutoIncDataStrategy(ColumnConfig columnConfig) {
        this.baseVal = columnConfig.getBaseValue();
        this.originVal = columnConfig.getBaseValue();
    }
}