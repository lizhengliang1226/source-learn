package com.lzl.datagenerator.strategy;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.log.Log;
import com.lzl.datagenerator.config.CacheManager;
import com.lzl.datagenerator.config.ColumnConfig;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
@ToString
public class DictValueDataStrategy implements DataStrategy {
    private final String colName;
    private final String dictColName;
    private final String dataSourceId;

    @Override
    public Object getNextVal() {
        Map<String, List<Object>> dictCache = CacheManager.getInstance().get(dataSourceId);
        if (dictCache == null) {
            Log.get().error("数据源ID[{}]列名[{}]配置了字典缓存策略但是未启用字典缓存或字典缓存不存在，请检查!", dataSourceId, colName);
            throw new RuntimeException();
        }
        List<Object> list = dictCache.get(dictColName == null ? colName : dictColName);
        if (CollectionUtil.isEmpty(list)) {
            Log.get().error("数据源ID[{}]列名[{}]的字典缓存不存在，请检查!", dataSourceId, dictColName == null ? colName : dictColName);
            throw new RuntimeException();
        }
        return RandomUtil.randomEle(list);
    }

    @Override
    public String getName() {
        return "dict-value";
    }


    public DictValueDataStrategy(ColumnConfig columnConfig) {
        this.colName = columnConfig.getColName();
        this.dictColName = columnConfig.getDictColName();
        this.dataSourceId = columnConfig.getDataSourceId();
    }
}