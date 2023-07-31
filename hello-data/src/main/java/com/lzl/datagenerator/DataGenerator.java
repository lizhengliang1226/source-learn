package com.lzl.datagenerator;

import cn.hutool.core.lang.Pair;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.db.ds.DSFactory;
import cn.hutool.db.meta.*;
import cn.hutool.log.Log;
import com.google.common.collect.Lists;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.lzl.datagenerator.GlobalSetting.dataCount;

/**
 * 版权声明：本程序模块属于后台业务系统（FSPT）的一部分
 * 金证科技股份有限公司 版权所有<br>
 * <p>
 * 模块名称：期权业务-<br>
 * 模块描述：期权业务-<br>
 * 开发作者：李正良<br>
 * 创建日期：2023/07/04<br>
 * 模块版本：1.0.0.0<br>
 * ----------------------------------------------------------------<br>
 * 修改日期      版本       作者      备注<br>
 * 2023/07/04   1.0.0.0   李正良      创建<br>
 * -----------------------------------------------------------------</p>
 */
public class DataGenerator {
    private String DEL_TABLE_TMPL = "TRUNCATE TABLE %s";

    public void test() {
        List<Pair<String, List<Entity>>> result = GlobalSetting.tableNames.stream().map(tableCode -> {
            Table tableInfo = MetaUtil.getTableMeta(DSFactory.get(), tableCode);
            // 唯一索引和主键去重后的列名集合，包含在里面的就要自己定义生成器生产数据
            Set<String> uniqueIndexColAndPkSet = Stream.concat(tableInfo.getIndexInfoList()
                                                                        .stream()
                                                                        .filter(index -> !index.isNonUnique())
                                                                        .flatMap(index -> index.getColumnIndexInfoList().stream())
                                                                        .map(ColumnIndexInfo::getColumnName)
                                                                        .collect(Collectors.toSet()).stream(), tableInfo.getPkNames().stream())
                                                       .collect(Collectors.toSet());

            List<Entity> res = new ArrayList<>();
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
                    // 从上往下取值，优先级从高到低
                    // 数据生成器取值
                    Object nextVal = ColInfoGenerator.getNextVal(colName);
                    // 字段默认值
                    Object colDefaultVal = ColInfoGenerator.getDefaultValByColName(colName);
                    // 取字典值
                    Object dictDefaultVal = ColInfoGenerator.getDictValByColName(colName);
                    // 类型默认值
                    Object typeDefaultVal = ColInfoGenerator.getDefaultVal(typeEnum);
                    if (uniqueIndexColAndPkSet.contains(colName) && nextVal == null) {
                        Log.get().error("表[{}]主键列或唯一索引列[{}]没有数据生成器，请检查!", tableCode, colName);
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
            ColInfoGenerator.reset();
            return Pair.of(tableCode, res);
        }).toList();
        // 保存数据
        result.parallelStream().forEach(dataPair -> {
            Log.get().info("开始删除表{}数据.", dataPair.getKey());
            try {
                Db.use().execute(String.format(DEL_TABLE_TMPL, dataPair.getKey()));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            Log.get().info("开始保存表{}数据，预计保存数据{}条.", dataPair.getKey(), dataPair.getValue().size());
            Lists.partition(dataPair.getValue(), 5000).parallelStream().forEach(list -> {
                try {
                    Db.use().insert(list);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }
}