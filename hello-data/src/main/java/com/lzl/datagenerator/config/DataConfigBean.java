package com.lzl.datagenerator.config;

import cn.hutool.db.Db;
import cn.hutool.setting.yaml.YamlUtil;
import lombok.Data;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
@Data
public class DataConfigBean {
    private List<ColumnConfig> columnConfig;
    private List<TableConfig> tableConfig;
    private DictConfig dictConfig;
    private String loadDictCache;
    private Map<String, String> colDefaultValue;
    private boolean loadCacheFlag = true;
    private Map<Object, List<Object>> dictCache = new HashMap<>(16);
    private List<Map<String, String>> patterns = new ArrayList<>();
    private volatile static DataConfigBean DATA_CONFIG_BEAN;

    public static DataConfigBean getInstance() {
        // 第一次检查，避免不必要的同步
        if (DATA_CONFIG_BEAN == null) {
            synchronized (DataConfigBean.class) {
                // 第二次检查，保证只有一个实例被创建
                if (DATA_CONFIG_BEAN == null) {
                    DATA_CONFIG_BEAN = YamlUtil.loadByPath("classpath:/generate.yml", DataConfigBean.class);
                    if (Boolean.TRUE.toString().equals(DATA_CONFIG_BEAN.loadDictCache)) {
                        DATA_CONFIG_BEAN.loadDictCache();
                    }
                    DATA_CONFIG_BEAN.resolveExp();
                }
            }
        }
        return DATA_CONFIG_BEAN;
    }

    private DataConfigBean() {

    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private Map<Pattern, Object> patternMap = new HashMap<>(16);
    private Map<String, Object> configParam = new HashMap<>(16) {{
        put("$sysdate", DATE_TIME_FORMATTER.format(LocalDate.now()));
    }};

    private void resolveExp() {
        patternMap = patterns.stream().flatMap(patternMap -> {
            String reg = patternMap.get("reg");
            String value = patternMap.get("value");
            Map<Pattern, Object> map = new HashMap<>(1);
            map.put(Pattern.compile(reg), configParam.getOrDefault(value, value));
            return map.entrySet().stream();
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void loadDictCache() {
        try {
            dictCache = Db.use().findAll(dictConfig.getDictTableName())
                          .stream().collect(Collectors.groupingBy(entity -> entity.get(dictConfig.getDictCodeColName()),
                                                                  Collectors.mapping(entity -> entity.get(dictConfig.getDictItemColName()),
                                                                                     Collectors.toList())));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(String.format("字典缓存加载失败，异常信息：%s", e.getMessage()));
        }
    }

}