package com.lzl.datagenerator;

import cn.hutool.setting.Setting;

import java.util.Arrays;
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
}
