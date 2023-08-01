package com.lzl;

import cn.hutool.setting.yaml.YamlUtil;
import com.lzl.datagenerator.config.DataConfigBean;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest
        extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        DataConfigBean configBean = YamlUtil.loadByPath("classpath:/generate.yml", DataConfigBean.class);
        System.out.println(configBean);
    }


}