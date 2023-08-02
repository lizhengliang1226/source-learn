package com.lzl;

import com.lzl.datagenerator.ColDataGenerator;
import com.lzl.datagenerator.DataGenerator;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        ColDataGenerator colDataGenerator = new ColDataGenerator();
        colDataGenerator.init();
        DataGenerator dataGenerator = new DataGenerator(colDataGenerator);
        dataGenerator.generate();
    }
}