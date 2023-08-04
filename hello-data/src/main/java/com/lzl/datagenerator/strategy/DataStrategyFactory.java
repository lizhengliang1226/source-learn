package com.lzl.datagenerator.strategy;

import com.lzl.datagenerator.config.ColumnConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
public class DataStrategyFactory {
    private static final Map<String, Function<ColumnConfig, DataStrategy>> STRATEGY_MAP = new HashMap<>();

    // 添加策略映射关系
    static {
        STRATEGY_MAP.put("fixed-value", FixedValueDataStrategy::new);
        STRATEGY_MAP.put("auto-inc", AutoIncDataStrategy::new);
        STRATEGY_MAP.put("rand-ele", RandomEleDataStrategy::new);
        STRATEGY_MAP.put("rand-table-ele", RandomTableEleDataStrategy::new);
        STRATEGY_MAP.put("dict-value", DictValueDataStrategy::new);
        STRATEGY_MAP.put("default", DefaultDataStrategy::new);
    }

    public static DataStrategy createDataStrategy(String strategyName, ColumnConfig columnConfig) {
        // 根据策略名称从映射表中获取创建策略对象的方法，并进行调用
        return STRATEGY_MAP.getOrDefault(strategyName, DefaultDataStrategy::new).apply(columnConfig);
    }
}