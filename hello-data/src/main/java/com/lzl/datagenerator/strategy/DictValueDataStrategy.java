package com.lzl.datagenerator.strategy;

import cn.hutool.core.util.RandomUtil;

import static com.lzl.datagenerator.ColInfoGenerator.DICT_CACHE;

/**
 * 版权声明：本程序模块属于后台业务系统（FSPT）的一部分
 * 金证科技股份有限公司 版权所有<br>
 * <p>
 * 模块名称：期权业务-<br>
 * 模块描述：期权业务-<br>
 * 开发作者：李正良<br>
 * 创建日期：2023/08/01<br>
 * 模块版本：1.0.0.0<br>
 * ----------------------------------------------------------------<br>
 * 修改日期      版本       作者      备注<br>
 * 2023/08/01   1.0.0.0   李正良      创建<br>
 * -----------------------------------------------------------------</p>
 */
public class DictValueDataStrategy implements DataStrategy {
    private final String colName;

    @Override
    public Object getNextVal() {
        return RandomUtil.randomEle(DICT_CACHE.get(colName));
    }

    @Override
    public void reset() {

    }

    @Override
    public String getName() {
        return "dict-value";
    }


    public DictValueDataStrategy(String colName) {
        this.colName = colName;
    }
}