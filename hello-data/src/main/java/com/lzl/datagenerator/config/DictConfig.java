package com.lzl.datagenerator.config;

import lombok.Data;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
@Data
public class DictConfig {
    private String dictTableName;
    private String dictCodeColName;
    private String dictItemColName;
}