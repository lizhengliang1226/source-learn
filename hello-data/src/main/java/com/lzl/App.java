package com.lzl;

import com.lzl.datagenerator.ColDataGenerator;
import com.lzl.datagenerator.DataGenerator;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        DataGenerator dataGenerator = new DataGenerator(new ColDataGenerator());
        dataGenerator.generate();
    }
}