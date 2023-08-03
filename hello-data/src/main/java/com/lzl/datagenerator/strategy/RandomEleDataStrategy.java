package com.lzl.datagenerator.strategy;

import cn.hutool.core.util.RandomUtil;
import com.lzl.datagenerator.config.ColumnConfig;
import lombok.ToString;

import java.util.List;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
@ToString
public class RandomEleDataStrategy implements DataStrategy {
    private final List<Object> randomList;
    @Override
    public Object getNextVal() {
        return RandomUtil.randomEle(randomList);
    }

    @Override
    public String getName() {
        return "rand-ele";
    }

    public RandomEleDataStrategy(ColumnConfig columnConfig) {
        this.randomList = columnConfig.getRandomEle();
    }
}