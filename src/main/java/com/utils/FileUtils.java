package com.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class FileUtils {

    /**
     * 讲键值对存储到文件 Properties->File
     * @param prop
     * @param fileName
     */
    public static void storePropToFile(Properties prop, String fileName){
        try(FileOutputStream out = new FileOutputStream(fileName)){
            prop.store(out, null);
//            System.out.println("save properties:");
//            System.out.println(prop);
//            System.out.println("save success!");
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println(fileName + " save failed!");
            System.exit(-1);
        }
    }

    /**
     * 从properties配置文件中获取数据
     * @param fileName
     * @return
     */
    public static Properties loadPropFromFile(String fileName) {
        Properties prop = new Properties();
        try (FileInputStream in = new FileInputStream(fileName)){
            prop.load(in);
        }
        catch (IOException e){
            e.printStackTrace();
            System.out.println(fileName + " load failed!");
            System.exit(-1);
        }
        return prop;
    }

    /**
     * 生成Properties对象
     * @param key
     * @param value
     * @return
     */
    public static Properties GenerateProperties(String key,String value){
        Properties prop = new Properties();
        prop.setProperty(key,value);
       // System.out.println("current properties : \n" + prop);
        return prop;
    }


    /**
     * 生成properties对象
     * @param m
     * @return
     */
    public static Properties GenerateProperties(Map<String,String> m){
        Properties prop = new Properties();

        Set<String> keys = m.keySet();
        for (String k : keys) {
            prop.setProperty(k, m.get(k));
        }
        //System.out.println("current properties : \n" + prop);
        return prop;
    }


}
