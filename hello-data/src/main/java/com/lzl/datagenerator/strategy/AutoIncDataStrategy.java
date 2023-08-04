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
public class AutoIncDataStrategy implements DataStrategy {
    private final AtomicLong baseVal;
    @Override
    public Object getNextVal() {
        return baseVal.getAndIncrement();
    }

    @Override
    public String getName() {
        return "auto-inc";
    }

    public AutoIncDataStrategy(ColumnConfig columnConfig) {
        this.baseVal =new AtomicLong(columnConfig.getBaseValue().longValue());
    }
}