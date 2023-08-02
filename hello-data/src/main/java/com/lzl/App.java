package com.lzl;

import com.lzl.datagenerator.ColDataGenerator;
import com.lzl.datagenerator.DataGenerator;

import java.sql.SQLException;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws SQLException {
        ColDataGenerator colDataGenerator = new ColDataGenerator();
        colDataGenerator.init();
        DataGenerator dataGenerator = new DataGenerator(colDataGenerator);
        dataGenerator.generate();
    }
}