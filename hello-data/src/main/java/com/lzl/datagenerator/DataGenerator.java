package com.lzl.datagenerator;

import cn.hutool.aop.ProxyUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.db.ds.DSFactory;
import cn.hutool.db.meta.*;
import cn.hutool.log.Log;
import com.google.common.collect.Lists;
import com.lzl.datagenerator.config.CacheManager;
import com.lzl.datagenerator.config.ColumnConfig;
import com.lzl.datagenerator.config.Configuration;
import com.lzl.datagenerator.config.DataConfigBean;
import com.lzl.datagenerator.proxy.ColDataProvider;
import com.lzl.datagenerator.proxy.ColDataProviderProxyImpl;
import com.lzl.datagenerator.strategy.DataStrategy;
import com.lzl.datagenerator.strategy.DataStrategyFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
public class DataGenerator {
    private final String DEL_TABLE_TMPL = "TRUNCATE TABLE %s";
    private final Configuration configuration;

    public DataGenerator(Configuration configuration) {
        this.configuration = configuration;
    }

    public void generate() {
        configuration.getDatasourceGroupList().parallelStream().forEach(dataConfigBean -> {
            List<Pair<String, List<Entity>>> result = dataConfigBean.getTableConfig()
                                                                    .parallelStream()
                                                                    .filter(this::checkTableConfig)
                                                                    .map(tableConfig -> {
                                                                        String[] split = tableConfig.split(":");
                                                                        String tableCode = split[0];
                                                                        // 构建数据集
                                                                        List<Entity> res;
                                                                        try {
                                                                            res = generateDataList(tableCode, Long.valueOf(split[1]),
                                                                                                   dataConfigBean);
                                                                        } catch (Exception e) {
                                                                            Log.get().error("生成表{}数据失败，表配置为{}", tableCode, tableConfig);
                                                                            throw e;
                                                                        }
                                                                        return Pair.of(tableCode, res);
                                                                    })
                                                                    .toList();
            // 保存数据，先全表删除，再全量插入
            saveData(result, dataConfigBean.getGroupName());
        });

    }

    private boolean checkTableConfig(String tableConfig) {
        String[] s = tableConfig.trim().split(":");
        if (s.length == 1) {
            Log.get().error("表{}未配置生成数据量", s[0]);
            throw new RuntimeException();
        }
        if (s.length == 2) {
            try {
                long l = Long.parseLong(s[1]);
            } catch (Exception e) {
                Log.get().error("表{}生成数据量配置错误，{}不是一个合法的数字", s[0], s[1]);
                throw new RuntimeException();
            }
            return true;
        }
        String notGen = "0";
        if (!notGen.equals(s[2])) {
            Log.get().warn("表{}配置了不生成数据标志，但是配置项不合法(只能为0)，将默认生成该表数据", s[0]);
            return true;
        } else {
            return false;
        }
    }

    private Set<String> getUniqueIndexCol(Table tableInfo) {
        return Stream.concat(tableInfo.getIndexInfoList()
                                      .stream()
                                      .filter(index -> !index.isNonUnique())
                                      .flatMap(index -> index.getColumnIndexInfoList().stream())
                                      .map(ColumnIndexInfo::getColumnName)
                                      .collect(Collectors.toSet()).stream(), tableInfo.getPkNames().stream())
                     .collect(Collectors.toSet());
    }

    /**
     * 创建数据集合
     *
     * @param tableCode  表代码
     * @param dataCount  要生成的数据量
     * @param dataConfig 数据源id
     * @return 生成的数据集合
     */
    private List<Entity> generateDataList(String tableCode, Long dataCount, DataConfigBean dataConfig) {
        List<Entity> res = new ArrayList<>();
        Table tableInfo = MetaUtil.getTableMeta(DSFactory.get(dataConfig.getGroupName()), tableCode);
        createTableColDataProvider(tableCode, tableInfo.getColumns(), dataConfig.getColumnConfigMap());
        // 唯一索引和主键去重后的列名集合，包含在里面的就要自己定义生成器生产数据
        Set<String> uniqueIndexColAndPkSet = getUniqueIndexCol(tableInfo);
        for (int i = 0; i < dataCount; i++) {
            Entity entity = Entity.create(tableCode);
            for (Column column : tableInfo.getColumns()) {
                if ("PARTITION_FIELD".equals(column.getName())) {
                    continue;
                }
                // 类型
                JdbcType typeEnum = column.getTypeEnum();
                // 列名
                String colName = column.getName();
                // 从上往下取值，优先级从高到低，数据生成策略>默认值>字典值>类型默认值
                // 数据生成策略器取值
                Object nextVal = getNextVal(tableCode, colName);
                // 字段默认值
                Object colDefaultVal = dataConfig.getColDefaultValue().get(colName);
                // 取字典值
                Object dictDefaultVal = getDictValByColName(colName, dataConfig.getGroupName());
                // 类型默认值
                Object typeDefaultVal = getDefaultValByJdbcType(typeEnum);
                if (uniqueIndexColAndPkSet.contains(colName) && nextVal == null && colDefaultVal == null && dictDefaultVal == null) {
                    Log.get().error("表[{}]主键列或唯一索引列[{}]没有配置数据生成策略或默认值，请检查!", tableCode, colName);
                    throw new RuntimeException();
                }
                // 根据优先级取值
                // 生成器 > 字段默认值 > 字典值 > 类型默认值
                entity.set(colName, nextVal == null ?
                        colDefaultVal == null ?
                                dictDefaultVal == null ?
                                        typeDefaultVal : dictDefaultVal : colDefaultVal : nextVal);
            }
            res.add(entity);
        }
        return res;
    }

    /**
     * 保存数据，根据传入的数据源id保存数据到该数据源
     *
     * @param result       要保存的数据
     * @param dataSourceId 数据源id
     */
    private void saveData(List<Pair<String, List<Entity>>> result, String dataSourceId) {
        result.parallelStream().forEach(dataPair -> {
            if (CollectionUtil.isNotEmpty(dataPair.getValue())) {
                Log.get().info("开始删除表{}数据.", dataPair.getKey());
                try {
                    Db.use(dataSourceId).execute(String.format(DEL_TABLE_TMPL, dataPair.getKey()));
                } catch (SQLException e) {
                    Log.get().error("删除表[{}]数据失败", dataPair.getKey());
                    throw new RuntimeException(e);
                }
                Log.get().info("开始保存表{}数据，预计保存数据{}条.", dataPair.getKey(), dataPair.getValue().size());
                Lists.partition(dataPair.getValue(), 5000).parallelStream().forEach(list -> {
                    try {
                        Db.use(dataSourceId).insert(list);
                    } catch (SQLException e) {
                        Log.get().error("保存表[{}]数据失败", dataPair.getKey());
                        throw new RuntimeException(e);
                    }
                });
            } else {
                Log.get().info("表{}生成的数据量为0，不生成数据", dataPair.getKey());
            }
        });
    }

    private static final Object DEFAULT_VAL = 0;
    private static final Map<JdbcType, Object> TYPE_DEFAULT_VAL_MAP = new HashMap<>() {{
        put(JdbcType.VARCHAR, "1");
        put(JdbcType.CHAR, "1");
        put(JdbcType.NUMERIC, 1);
        put(JdbcType.TIMESTAMP, LocalDateTime.now());
        put(JdbcType.INTEGER, 1);
    }};
    private final Map<String, Map<String, ColDataProvider>> tableColDataProviderMap = new HashMap<>(16);

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


    public Object getDictValByColName(String colName, String dataSourceId) {
        try {
            List<Object> dictItems = CacheManager.getInstance().get(dataSourceId).get(colName);
            if (dictItems != null) {
                return RandomUtil.randomEle(dictItems);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * 创建表的列数据生成器，生成一个map存入成员变量
     *
     * @param tableCode       表代码
     * @param columns         表的元数据列
     * @param columnConfigMap 表的列配置信息
     */
    public void createTableColDataProvider(String tableCode, Collection<Column> columns, Map<String, ColumnConfig> columnConfigMap) {
        Map<String, ColDataProvider> colDataProviderMap = columns.parallelStream()
                                                                 .filter(
                                                                         column -> columnConfigMap.containsKey(column.getName()))
                                                                 .map(
                                                                         column -> createColDataProvider(columnConfigMap.get(column.getName())))
                                                                 .collect(Collectors.toMap(ColDataProvider::getName,
                                                                                           colDataProvider -> colDataProvider));
        tableColDataProviderMap.put(tableCode, colDataProviderMap);
    }

    private ColDataProvider createColDataProvider(ColumnConfig columnConfig) {
        return getColDataProxy(columnConfig.getColName(),
                               DataStrategyFactory.createDataStrategy(
                                       columnConfig.getStrategy(),
                                       columnConfig));

    }
}