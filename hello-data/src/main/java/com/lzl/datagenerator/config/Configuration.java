package com.lzl.datagenerator.config;

import cn.hutool.setting.yaml.YamlUtil;
import lombok.Data;

import java.util.List;

/**
 * 配置类
 *
 * @author LZL
 * @version v1.0
 * @date 2023/8/4-21:18
 */
@Data
public class Configuration {
    private String generate;
    private List<DataConfigBean> datasourceGroupList;
    private static volatile Configuration CONFIGURATION;

    public static Configuration getInstance() {
        // 第一次检查，避免不必要的同步
        if (CONFIGURATION == null) {
            synchronized (DataConfigBean.class) {
                // 第二次检查，保证只有一个实例被创建
                if (CONFIGURATION == null) {
                    CONFIGURATION = YamlUtil.loadByPath("classpath:/config/generate.yml", Configuration.class);
                    CONFIGURATION.getDatasourceGroupList()
                                 .parallelStream()
                                 .filter(config -> CONFIGURATION.getGenerate().equals("ALL") || CONFIGURATION.getGenerate()
                                                                                                             .contains(config.getDataSourceId()))
                                 .forEach(DataConfigBean::init);
                }
            }
        }
        return CONFIGURATION;
    }

    private Configuration() {
    }
}