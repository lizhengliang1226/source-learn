package com.lzl.datagenerator.strategy;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.db.Db;
import cn.hutool.log.Log;
import com.lzl.datagenerator.config.ColumnConfig;
import lombok.ToString;

import java.sql.SQLException;
import java.util.List;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
@ToString
public class RandomTableEleDataStrategy implements DataStrategy {

    private final List<Object> randomList;

    @Override
    public Object getNextVal() {
        return RandomUtil.randomEle(randomList);
    }

    @Override
    public String getName() {
        return "rand-table-ele";
    }

    public RandomTableEleDataStrategy(ColumnConfig columnConfig) {
        try {
            randomList = Db.use().query(columnConfig.getQuerySql()).stream().map(e -> e.get(columnConfig.getQueryCol())).distinct().toList();
        } catch (SQLException e) {
            Log.get().error("构建rand-table-ele策略异常，查询SQL[{}]查询字段[{}]",columnConfig.getQuerySql(),columnConfig.getQueryCol());
            throw new RuntimeException(e);
        }
    }
}