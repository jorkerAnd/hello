package com.mmall.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.InputStreamReader;
import java.util.Properties;
//todo 配置资料的加载类
@Slf4j
public class PropertiesUtils {

    private static Properties props;

    static {
        String fileName = "mmall.properties";
        props = new Properties();
        try {
            props.load(new InputStreamReader(PropertiesUtils.class.getClassLoader().getResourceAsStream(fileName), "UTF-8"));
        } catch (Exception e) {
            log.error("配置文件异常", e);
        }
    }


    public static String getProperty(String key) {
        String value = props.getProperty(key.trim());
        if (StringUtils.isBlank(value))
            return null;
        return value.trim();
    }


    public static String getProperty(String key,String defaultValue) {
        String value = props.getProperty(key.trim());
        if (StringUtils.isBlank(value))
            return defaultValue.trim();
        return value.trim();
    }


    public static void main(String[] args) {
    System.out.print( PropertiesUtils.getProperty("ftp.user"));
    }
}
