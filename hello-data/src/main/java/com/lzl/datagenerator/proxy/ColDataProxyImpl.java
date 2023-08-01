package com.lzl.datagenerator.proxy;

import com.lzl.datagenerator.strategy.DataStrategy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

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
public class ColDataProxyImpl implements InvocationHandler {
    private final String colName;
    private final DataStrategy strategy;

    public ColDataProxyImpl(String colName, DataStrategy strategy) {
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
            case "reset" -> strategy.reset();
        }
        return null;
    }

}