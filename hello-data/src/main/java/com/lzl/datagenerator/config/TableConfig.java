package com.lzl.datagenerator.config;

import lombok.Data;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
@Data
public class TableConfig {
    private String tableName;
    private Long dataCount;
    private int genFlag;
}