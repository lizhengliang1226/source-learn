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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;


/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
public class DataGenerator {
    private final Configuration configuration;

    public DataGenerator(Configuration configuration) {
        this.configuration = configuration;
    }

    public void generate() {
        configuration.getDatasourceGroupList()
                     .parallelStream()
                     .filter(config -> "ALL".equals(configuration.getGenerate()) || configuration.getGenerate().contains(config.getDataSourceId()))
                     .forEach(dataConfigBean -> dataConfigBean.getTableConfig()
                                                              .parallelStream()
                                                              .filter(this::checkTableConfig)
                                                              .map(tableConfig -> generateDataList(tableConfig, dataConfigBean))
                                                              .forEach(dataPair -> saveData(dataConfigBean, dataPair)));

    }

    private void saveData(DataConfigBean dataConfigBean, Pair<String, List<Entity>> dataPair) {
        if (CollectionUtil.isNotEmpty(dataPair.getValue())) {
            Log.get().info("开始删除表{}数据.", dataPair.getKey());
            try {
                String DEL_TABLE_TMPL = "TRUNCATE TABLE %s";
                Db.use(dataConfigBean.getDataSourceId()).execute(String.format(DEL_TABLE_TMPL, dataPair.getKey()));
            } catch (SQLException e) {
                Log.get().error("删除表[{}]数据失败", dataPair.getKey());
                throw new RuntimeException(e);
            }
            Log.get().info("开始保存表{}数据，预计保存数据{}条.", dataPair.getKey(), dataPair.getValue().size());
            Lists.partition(dataPair.getValue(), 5000).parallelStream().forEach(list -> {
                try {
                    Db.use(dataConfigBean.getDataSourceId()).insert(list);
                } catch (SQLException e) {
                    Log.get().error("保存表[{}]数据失败，原因[{}]", dataPair.getKey(),e.getMessage());
                    throw new RuntimeException(e);
                }
            });
        } else {
            Log.get().info("表{}生成的数据量为0，不生成数据", dataPair.getKey());
        }
    }

    /**
     * 检查表配置
     *
     * @param tableConfig 表配置
     * @return true 如果配置正确
     */
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

    /**
     * 根据表的元数据信息获取表的唯一索引和主键的列集合
     *
     * @param tableInfo 表的元数据信息
     * @return 主键和唯一索引列集合
     */
    private Set<String> getUniqueIndexCol(Table tableInfo) {
        return Stream.concat(tableInfo.getIndexInfoList()
                                      .parallelStream()
                                      .filter(index -> !index.isNonUnique())
                                      .flatMap(index -> index.getColumnIndexInfoList().parallelStream())
                                      .map(ColumnIndexInfo::getColumnName)
                                      .collect(Collectors.toSet()).parallelStream(), tableInfo.getPkNames().parallelStream())
                     .collect(Collectors.toSet());
    }

    /**
     * 创建数据集合
     *
     * @param dataConfig 数据源id
     * @return 生成的数据集合
     */
    private Pair<String, List<Entity>> generateDataList(String tableConfig, DataConfigBean dataConfig) {
        String[] split = tableConfig.split(":");
        String tableCode = split[0];
        Table tableInfo = MetaUtil.getTableMeta(DSFactory.get(dataConfig.getDataSourceId()), tableCode);
        createTableColDataProvider(tableCode, tableInfo.getColumns(), dataConfig.getColumnConfigMap());
        // 唯一索引和主键去重后的列名集合，包含在里面的就要自己定义生成器生产数据
        Set<String> uniqueIndexColAndPkSet = getUniqueIndexCol(tableInfo);
        List<Entity> res = LongStream.range(0L, Long.parseLong(split[1]))
                                     .parallel()
                                     .mapToObj(index -> Entity.create(tableCode))
                                     .peek(e -> tableInfo.getColumns()
                                                         .forEach(column -> setColumnValue(dataConfig, tableCode, uniqueIndexColAndPkSet, e, column)))
                                     .toList();
        return Pair.of(tableCode, res);
    }

    private void setColumnValue(DataConfigBean dataConfig, String tableCode, Set<String> uniqueIndexColAndPkSet, Entity entity, Column column) {
        if ("PARTITION_FIELD".equals(column.getName())) {
            return;
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
        Object dictDefaultVal = getDictValByColName(colName, dataConfig.getDataSourceId());
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

    private static final Integer DEFAULT_VAL = 0;
    private static final Map<JdbcType, Object> TYPE_DEFAULT_VAL_MAP = new HashMap<>() {{
        put(JdbcType.VARCHAR, "1");
        put(JdbcType.CHAR, "1");
        put(JdbcType.NUMERIC, 1);
        put(JdbcType.TIMESTAMP, LocalDateTime.now());
        put(JdbcType.INTEGER, 1);
    }};
    private final Map<String, Map<String, ColDataProvider>> tableColDataProviderMap = new ConcurrentHashMap<>(16);

    /**
     * 获取列数据生成器的代理实现
     *
     * @param colName  列名
     * @param strategy 策略名
     * @return 根据配置生成的代理实现
     */
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

    /**
     * 获取默认值通过JDBC类型
     *
     * @param jdbcType jdbc类型
     * @return JDBC类型的默认值
     */
    public Object getDefaultValByJdbcType(JdbcType jdbcType) {
        Object defaultVal = TYPE_DEFAULT_VAL_MAP.get(jdbcType);
        if (defaultVal == null) {
            Log.get().error("数据类型{}没有默认值设置，请检查!", jdbcType.name());
            throw new RuntimeException();
        }
        return defaultVal;
    }

    /**
     * 尝试从字典缓存获取值，获取不到返回空，不报错
     *
     * @param colName      列名
     * @param dataSourceId 数据源ID
     * @return 获取到的字典值
     */
    public Object getDictValByColName(String colName, String dataSourceId) {
        try {
            Map<String, List<Object>> dictCache = CacheManager.getInstance().get(dataSourceId);
            List<Object> dictItems = dictCache.get(colName);
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

    /**
     * 根据列配置创建列数据生成器
     *
     * @param columnConfig 列配置
     * @return 列数据生成器
     */
    private ColDataProvider createColDataProvider(ColumnConfig columnConfig) {
        return getColDataProxy(columnConfig.getColName(),
                               DataStrategyFactory.createDataStrategy(
                                       columnConfig.getStrategy(),
                                       columnConfig));

    }
}