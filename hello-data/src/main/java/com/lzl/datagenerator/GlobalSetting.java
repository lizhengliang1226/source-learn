package com.lzl.datagenerator;

import cn.hutool.aop.ProxyUtil;
import cn.hutool.setting.Setting;
import cn.hutool.setting.yaml.YamlUtil;
import com.lzl.datagenerator.config.DataConfigBean;
import com.lzl.datagenerator.proxy.ColData;
import com.lzl.datagenerator.proxy.ColDataProxyImpl;
import com.lzl.datagenerator.strategy.AutoIncDataStrategy;
import com.lzl.datagenerator.strategy.DataStrategy;
import com.lzl.datagenerator.strategy.FixedValueDataStrategy;
import com.lzl.datagenerator.strategy.RandomEleDataStrategy;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局配置加载
 *
 * @author LZL
 * @version v1.0
 * @date 2023/7/29-21:46
 */
public class GlobalSetting {
    public static int dataCount = 0;
    public static Set<String> tableNames;
    private static final String GEN_FLAG = "1";
    public static boolean loadDictCache;
    public static String dictTableName;
    public static String dictCodeColName;
    public static String dictItemColName;

    public static void createGlobalConfigInfo() {
        Setting setting = new Setting("generate.setting");
        dataCount = Integer.parseInt(setting.get("dataCount"));
        tableNames = Arrays.stream(setting.get("tableList").split(","))
                           .filter(table -> GEN_FLAG.equals(table.split(":")[1].trim()))
                           .map(table -> table.split(":")[0].trim())
                           .collect(Collectors.toSet());
        loadDictCache = setting.get("loadDictCache").equals("true");
        if (loadDictCache) {
            dictTableName = setting.get("dictTableName");
            dictCodeColName = setting.get("dictCodeColName");
            dictItemColName = setting.get("dictItemColName");
        }
    }

    static {
        createGlobalConfigInfo();
    }
    // private Map<String,>
    /**
     * 全局配置加载方法，所有的配置从此处加载
     */
    public void init(){
        DataConfigBean configBean = YamlUtil.loadByPath("classpath:/generate.yml", DataConfigBean.class);
        Map<String, ColData> collect = configBean.getColumnConfig().parallelStream().map(columnConfig -> {
            String strategyName = columnConfig.getStrategy();
            ColData colDataProxy = null;
            if ("fixed-value".equals(strategyName)) {
                DataStrategy strategy = new FixedValueDataStrategy(columnConfig.getFixedValue());
                colDataProxy = getColDataProxy(columnConfig.getColName(), strategy);
            } else if ("auto-inc".equals(strategyName)) {
                DataStrategy strategy = new AutoIncDataStrategy(columnConfig.getBaseValue());
                colDataProxy = getColDataProxy(columnConfig.getColName(), strategy);
            } else if ("rand-ele".equals(strategyName)) {
                DataStrategy strategy = new RandomEleDataStrategy(columnConfig.getRandomEle());
                colDataProxy = getColDataProxy(columnConfig.getColName(), strategy);
            } else if ("rand-table-ele".equals(strategyName)) {
                DataStrategy strategy = new FixedValueDataStrategy(columnConfig.getFixedValue());
                colDataProxy = getColDataProxy(columnConfig.getColName(), strategy);
            } else if ("fixed-value".equals(strategyName)) {
                DataStrategy strategy = new FixedValueDataStrategy(columnConfig.getFixedValue());
                colDataProxy = getColDataProxy(columnConfig.getColName(), strategy);
            } else {
                DataStrategy strategy = new FixedValueDataStrategy(columnConfig.getFixedValue());
                colDataProxy = getColDataProxy(columnConfig.getColName(), strategy);
            }
            return colDataProxy;
        }).collect(Collectors.toMap(ColData::getName, colData -> colData));
        System.out.println(configBean);
    }
    public ColData getColDataProxy(String colName, DataStrategy strategy) {
        return ProxyUtil.newProxyInstance(new ColDataProxyImpl(colName, strategy), ColData.class);
    }
}