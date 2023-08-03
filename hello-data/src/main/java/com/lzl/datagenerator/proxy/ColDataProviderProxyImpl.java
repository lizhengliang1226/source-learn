package com.lzl.datagenerator.proxy;

import com.lzl.datagenerator.strategy.DataStrategy;
import lombok.Data;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author LZL
 * @version v1.0
 * @date 2023/7/31-22:24
 */
@Data
public class ColDataProviderProxyImpl implements InvocationHandler {
    /**
     * 列名
     */
    private final String colName;
    /**
     * 数据生成策略
     */
    private final DataStrategy strategy;

    public ColDataProviderProxyImpl(String colName, DataStrategy strategy) {
        this.colName = colName;
        this.strategy = strategy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        switch (name) {
            case "getName" -> {
                return colName;
            }
            case "getNextVal" -> {
                return strategy.getNextVal();
            }
            case "toString" -> {
                return toString();
            }
            default -> {
                throw new RuntimeException(String.format("没有方法%s的代理实现", name));
            }
        }
    }
}