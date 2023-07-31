package com.lzl.datagenerator;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.db.Db;
import cn.hutool.db.meta.JdbcType;
import cn.hutool.log.Log;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.lzl.datagenerator.GlobalSetting.*;

/**
 * 版权声明：本程序模块属于后台业务系统（FSPT）的一部分
 * 金证科技股份有限公司 版权所有<br>
 * <p>
 * 模块名称：期权业务-<br>
 * 模块描述：期权业务-<br>
 * 开发作者：李正良<br>
 * 创建日期：2023/07/28<br>
 * 模块版本：1.0.0.0<br>
 * ----------------------------------------------------------------<br>
 * 修改日期      版本       作者      备注<br>
 * 2023/07/28   1.0.0.0   李正良      创建<br>
 * -----------------------------------------------------------------</p>
 */
public class ColInfoGenerator {

    private static final Object DEFAULT_VAL = 0;
    private static final Map<JdbcType, Object> TYPE_DEFAULT_VAL_MAP = new HashMap<>() {{
        put(JdbcType.VARCHAR, "1");
        put(JdbcType.CHAR, "1");
        put(JdbcType.NUMERIC, 1);
        put(JdbcType.TIMESTAMP, LocalDateTime.now());
        put(JdbcType.INTEGER, 1);
    }};

    private static final Map<String, Object> COLUMN_DEFAULT_VAL_MAP = new HashMap<>() {{
        put("OPEN_DATE", 20020303);
        put("CLOSE_DATE", 20990101);
        put("CUACCT_ATTR", "3");
        put("CUACCT_STATUS", "0");
        put("INT_ORG", 0);
    }};


    private static final Pattern DATE_PATTERN = Pattern.compile("^.*_DATE$");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Map<Pattern, Object> PATTERN_MAP = new HashMap<>(16) {{
        put(DATE_PATTERN, DATE_TIME_FORMATTER.format(LocalDate.now()));
    }};

    private static final Map<String, ColData> COL_DATA_MAP = new HashMap<>(16);
    public static Map<Object, List<Object>> DICT_CACHE = new HashMap<>(16);

    static {
        // 加载字段数据生成器
        ServiceLoader.load(ColData.class).stream().forEach(colData -> {
            COL_DATA_MAP.put(colData.get().getName(), colData.get());
        });
        // 加载字典缓存
        try {
            if (loadDictCache) {
                DICT_CACHE = Db.use().findAll(dictTableName)
                               .stream().collect(Collectors.groupingBy(entity -> entity.get(dictCodeColName),
                                                                       Collectors.mapping(entity -> entity.get(dictItemColName), Collectors.toList())));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getNextVal(String colName) {
        ColData colData = COL_DATA_MAP.get(colName);
        if (colData == null) {
            return null;
        }
        return colData.getNextVal();
    }

    public static Object getDefaultVal(JdbcType jdbcType) {
        Object defaultVal = TYPE_DEFAULT_VAL_MAP.get(jdbcType);
        if (defaultVal == null) {
            Log.get().error("数据类型{}没有默认值设置，请检查!", jdbcType.name());
            throw new RuntimeException();
        }
        return defaultVal;
    }

    public static Object getDefaultValByColName(String colName) {
        // 先从默认值map取，取不到进行模式匹配，匹配到就返回模式匹配的默认值
        Object val = COLUMN_DEFAULT_VAL_MAP.get(colName);
        if (val == null) {
            Optional<Object> patternVal = PATTERN_MAP.entrySet()
                                                     .stream()
                                                     .filter(e -> e.getKey().matcher(colName).find())
                                                     .map(Map.Entry::getValue)
                                                     .findAny();
            if (patternVal.isPresent()) {
                return patternVal.get();
            }
        }
        return null;
    }

    public static void reset() {
        COL_DATA_MAP.values().parallelStream().forEach(ColData::reset);
    }

    public static Object getDictValByColName(String colName) {
        List<Object> dictItems = DICT_CACHE.get(colName);
        if (dictItems != null) {
            return RandomUtil.randomEle(dictItems);
        }
        return null;
    }
}