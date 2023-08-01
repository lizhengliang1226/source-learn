package com.lzl;

import com.lzl.datagenerator.GlobalSetting;

import java.sql.SQLException;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws SQLException {
        GlobalSetting globalSetting = new GlobalSetting();
        globalSetting.init();
        // new DataGenerator().generate();
    }
}