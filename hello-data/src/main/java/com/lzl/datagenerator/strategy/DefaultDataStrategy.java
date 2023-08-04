package com.lzl.datagenerator.strategy;

import com.lzl.datagenerator.config.ColumnConfig;
import lombok.ToString;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
@ToString
public class DefaultDataStrategy implements DataStrategy {
    private final AtomicLong baseVal = new AtomicLong(0L);

    public DefaultDataStrategy(ColumnConfig columnConfig) {

    }

    @Override
    public Object getNextVal() {
        return baseVal.getAndIncrement();
    }


    @Override
    public String getName() {
        return "default";
    }

    public DefaultDataStrategy() {

    }
}