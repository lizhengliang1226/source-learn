package com.lzl.datagenerator;

import cn.hutool.aop.ProxyUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.db.meta.Column;
import cn.hutool.db.meta.JdbcType;
import cn.hutool.log.Log;
import com.lzl.datagenerator.config.ColumnConfig;
import com.lzl.datagenerator.config.DataConfigBean;
import com.lzl.datagenerator.proxy.ColDataProvider;
import com.lzl.datagenerator.proxy.ColDataProviderProxyImpl;
import com.lzl.datagenerator.strategy.DataStrategy;
import com.lzl.datagenerator.strategy.DataStrategyFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
public class ColDataGenerator {

    private static final Object DEFAULT_VAL = 0;
    private static final Map<JdbcType, Object> TYPE_DEFAULT_VAL_MAP = new HashMap<>() {{
        put(JdbcType.VARCHAR, "1");
        put(JdbcType.CHAR, "1");
        put(JdbcType.NUMERIC, 1);
        put(JdbcType.TIMESTAMP, LocalDateTime.now());
        put(JdbcType.INTEGER, 1);
    }};
    private Map<String, Map<String, ColDataProvider>> tableColDataProviderMap = new HashMap<>(16);
    private final DataConfigBean dataConfigBean = DataConfigBean.getInstance();

    private ColDataProvider getColDataProxy(String colName, DataStrategy strategy) {
        return ProxyUtil.newProxyInstance(new ColDataProviderProxyImpl(colName, strategy), ColDataProvider.class);
    }

    /**
     * 获取某一列的下一个值
     *
     * @param colName   列名
     * @param tableCode 表名
     * @return 下一个值
     */
    public Object getNextVal(String tableCode, String colName) {
        ColDataProvider colDataProvider = tableColDataProviderMap.get(tableCode).get(colName);
        if (colDataProvider == null) {
            return null;
        }
        return colDataProvider.getNextVal();
    }

    public Object getDefaultValByJdbcType(JdbcType jdbcType) {
        Object defaultVal = TYPE_DEFAULT_VAL_MAP.get(jdbcType);
        if (defaultVal == null) {
            Log.get().error("数据类型{}没有默认值设置，请检查!", jdbcType.name());
            throw new RuntimeException();
        }
        return defaultVal;
    }

    public Object getDefaultValByColName(String colName) {
        // 先从默认值map取，取不到进行模式匹配，匹配到就返回模式匹配的默认值
        Object val = dataConfigBean.getColDefaultValue().get(colName);
        if (val == null) {
            Optional<Object> patternVal = dataConfigBean.getPatternMap().entrySet()
                                                        .stream()
                                                        .filter(e -> e.getKey().matcher(colName).find())
                                                        .map(Map.Entry::getValue)
                                                        .findAny();
            if (patternVal.isPresent()) {
                return patternVal.get();
            }
        }
        return val;
    }

    public Object getDictValByColName(String colName) {
        List<Object> dictItems = dataConfigBean.getDictCache().get(colName);
        if (dictItems != null) {
            return RandomUtil.randomEle(dictItems);
        }
        return null;
    }

    public void createTableColDataProvider(String tableCode, Collection<Column> columns) {
        Map<String, ColDataProvider> colDataProviderMap = columns.parallelStream().filter(
                column -> dataConfigBean.getColumnConfigMap().containsKey(column.getName())).map(
                col -> createColDataProvider(col.getName())).collect(Collectors.toMap(ColDataProvider::getName, colDataProvider -> colDataProvider));
        tableColDataProviderMap.put(tableCode, colDataProviderMap);
    }

    private ColDataProvider createColDataProvider(String colName) {
        ColumnConfig columnConfig = dataConfigBean.getColumnConfigMap().get(colName);
        return getColDataProxy(columnConfig.getColName(),
                               DataStrategyFactory.createDataStrategy(
                                       columnConfig.getStrategy(),
                                       columnConfig));

    }
}