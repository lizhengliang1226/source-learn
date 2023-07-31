package com.lzl;

import com.lzl.datagenerator.DataGenerator;

import java.sql.SQLException;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws SQLException {
        new DataGenerator().test();
    }
}