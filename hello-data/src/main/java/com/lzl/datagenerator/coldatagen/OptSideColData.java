package com.lzl.datagenerator.coldatagen;

import cn.hutool.core.util.RandomUtil;
import com.lzl.datagenerator.ColInfoGenerator;


/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
public class OptSideColData extends BaseColData {


    @Override
    public String getName() {
        return "OPT_SIDE";
    }


    @Override
    public Object getNextVal() {
        return RandomUtil.randomEle(ColInfoGenerator.DICT_CACHE.get("OPT_SIDE"));
    }

    @Override
    public void reset() {

    }
}