package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

//一般用的redis缓存，所以这一部分不需要多看
@Slf4j
/**
 * 建立本地的缓存
 */
public class TokenCahe {
    //LRU算法
    public static LoadingCache<String, String> localCache = CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                @Override//默认的数据加载，当调用get取值的时候，如果key没有对应的值，就调用这个方法进行加载.
                public String load(String s) throws Exception {
                    return "null";//因为会调用equals方法，如果为null的话就会出现空指针的异常。
                }
            });

    public static void setKey(String key, String value) {
        localCache.put(key, value);
    }

    public static String getKey(String key) {

        try {
            String value = localCache.get(key);
            if (value.equals("null"))
                return null;
            return value;
        } catch (Exception e) {
            log.error("localCache get error", e);
        }
        return null;
    }

    public static final String TOKEN_PREFIX = "token_";
}
