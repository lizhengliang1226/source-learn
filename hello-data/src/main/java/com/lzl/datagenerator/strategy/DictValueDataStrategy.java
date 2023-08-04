package com.lzl.datagenerator.strategy;

import cn.hutool.core.util.RandomUtil;
import com.lzl.datagenerator.config.ColumnConfig;
import com.lzl.datagenerator.config.DataConfigBean;
import lombok.ToString;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
@ToString
public class DictValueDataStrategy implements DataStrategy {
    private final String colName;
    private final String dictColName;

    @Override
    public Object getNextVal() {
        return RandomUtil.randomEle(DataConfigBean.getInstance().getDictCache().get(dictColName == null ? colName : dictColName));
    }

    @Override
    public String getName() {
        return "dict-value";
    }


    public DictValueDataStrategy(ColumnConfig columnConfig) {
        this.colName = columnConfig.getColName();
        this.dictColName = columnConfig.getDictColName();
    }
}