package com.lzl.datagenerator.config;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.db.Db;
import lombok.Data;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
@Data
public class DataConfigBean {
    private String dataSourceId;
    private List<ColumnConfig> columnConfig;
    private List<String> tableConfig;
    private DictConfig dictConfig;
    private String loadDictCache;
    private Map<String, String> colDefaultValue =new HashMap<>(16);
    private volatile static DataConfigBean DATA_CONFIG_BEAN;
    private Map<String, ColumnConfig> columnConfigMap=new HashMap<>(16);

    private void transColumnConfig() {
        if (CollectionUtil.isNotEmpty(columnConfig)) {
            columnConfigMap = columnConfig.parallelStream().flatMap(columnConfig -> {
                String colName = columnConfig.getColName();
                return Arrays.stream(colName.split(",")).flatMap(col -> {
                    Map<String, ColumnConfig> map = new HashMap<>();
                    ColumnConfig clone = ObjectUtil.clone(columnConfig);
                    clone.setColName(col);
                    clone.setDataSourceId(dataSourceId);
                    map.put(col, columnConfig);
                    return map.entrySet().stream();
                });
            }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

    }

    private DataConfigBean() {

    }

    private void loadDictCache() {
        try {
            CacheManager.getInstance().put(dataSourceId, Db.use(dataSourceId).findAll(dictConfig.getDictTableName())
                                                           .stream().collect(Collectors.groupingBy(entity -> entity.get(dictConfig.getDictCodeColName()),
                                                                                                Collectors.mapping(entity -> entity.get(
                                                                                                                           dictConfig.getDictItemColName()),
                                                                                                                   Collectors.toList()))));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(String.format("数据库ID为[%s]的字典缓存加载失败，异常信息：%s", dataSourceId, e.getMessage()));
        }
    }

    public void init() {
        if (Boolean.TRUE.toString().equals(loadDictCache)) {
            // 加载字典缓存
            loadDictCache();
        }
        // 转换列配置
        transColumnConfig();
    }
}