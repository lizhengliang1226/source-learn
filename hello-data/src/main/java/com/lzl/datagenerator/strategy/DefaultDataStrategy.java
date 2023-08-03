package com.lzl.datagenerator.strategy;

import com.lzl.datagenerator.config.ColumnConfig;
import lombok.ToString;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
@ToString
public class DefaultDataStrategy implements DataStrategy {
    private Long baseVal = 0L;

    public DefaultDataStrategy(ColumnConfig columnConfig) {

    }

    @Override
    public Object getNextVal() {
        return baseVal++;
    }


    @Override
    public String getName() {
        return "default";
    }

    public DefaultDataStrategy() {

    }
}