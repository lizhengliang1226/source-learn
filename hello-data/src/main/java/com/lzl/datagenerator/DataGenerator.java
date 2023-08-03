package com.lzl.datagenerator;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.db.ds.DSFactory;
import cn.hutool.db.meta.*;
import cn.hutool.log.Log;
import com.google.common.collect.Lists;
import com.lzl.datagenerator.config.DataConfigBean;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
public class DataGenerator {
    private String DEL_TABLE_TMPL = "TRUNCATE TABLE %s";
    private final DataConfigBean dataConfigBean = DataConfigBean.getInstance();
    private final ColDataGenerator colDataGenerator;
    private final String notGen = "0";

    public void generate() {
        // 此处必须串行流，否则会有线程安全问题
        List<Pair<String, List<Entity>>> result = dataConfigBean.getTableConfig().stream().filter(this::checkTableConfig).map(tableConfig -> {
            String[] split = tableConfig.split(":");
            String tableCode = split[0];
            Table tableInfo = MetaUtil.getTableMeta(DSFactory.get(dataConfigBean.getJdbcGroup()), tableCode);
            // 唯一索引和主键去重后的列名集合，包含在里面的就要自己定义生成器生产数据
            Set<String> uniqueIndexColAndPkSet = getUniqueIndexCol(tableInfo);
            // 构建数据集
            List<Entity> res = generateDataList(tableCode, tableInfo, uniqueIndexColAndPkSet, Long.valueOf(split[1]));
            return Pair.of(tableCode, res);
        }).toList();
        // 保存数据，先全表删除，再全量插入
        saveData(result);
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
        if (!notGen.equals(s[2])) {
            Log.get().warn("表{}配置了不生成数据标志，但是配置项不合法(只能为0)，默认生成该表数据", s[0]);
            return true;
        } else {
            return false;
        }
    }

    public DataGenerator(ColDataGenerator colDataGenerator) {
        this.colDataGenerator = colDataGenerator;
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

    private List<Entity> generateDataList(String tableCode, Table tableInfo, Set<String> uniqueIndexColAndPkSet, Long dataCount) {
        List<Entity> res = new ArrayList<>();
        colDataGenerator.createTableColDataProvider(tableCode,tableInfo.getColumns());
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
                Object nextVal = colDataGenerator.getNextVal(tableCode,colName);
                // 字段默认值
                Object colDefaultVal = colDataGenerator.getDefaultValByColName(colName);
                // 取字典值
                Object dictDefaultVal = colDataGenerator.getDictValByColName(colName);
                // 类型默认值
                Object typeDefaultVal = colDataGenerator.getDefaultValByJdbcType(typeEnum);
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
     * 保存数据
     *
     * @param result 要保存的数据
     */
    private void saveData(List<Pair<String, List<Entity>>> result) {
        result.parallelStream().forEach(dataPair -> {
            if (CollectionUtil.isNotEmpty(dataPair.getValue())) {
                Log.get().info("开始删除表{}数据.", dataPair.getKey());
                try {
                    Db.use(dataConfigBean.getJdbcGroup()).execute(String.format(DEL_TABLE_TMPL, dataPair.getKey()));
                } catch (SQLException e) {
                    Log.get().error("删除表[{}]数据失败", dataPair.getKey());
                    throw new RuntimeException(e);
                }
                Log.get().info("开始保存表{}数据，预计保存数据{}条.", dataPair.getKey(), dataPair.getValue().size());
                Lists.partition(dataPair.getValue(), 5000).parallelStream().forEach(list -> {
                    try {
                        Db.use(dataConfigBean.getJdbcGroup()).insert(list);
                    } catch (SQLException e) {
                        Log.get().error("保存表[{}]数据失败", dataPair.getKey());
                        throw new RuntimeException(e);
                    }
                });
            }else{
                Log.get().info("表{}生成的数据量为0，不生成数据",dataPair.getKey());
            }
        });
    }
}