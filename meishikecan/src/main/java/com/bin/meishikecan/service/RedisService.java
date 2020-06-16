package com.bin.meishikecan.service;

public interface RedisService {

    /**
     * 设置单个值字符串,并设置过期时间,为null则不设置,单位是秒
     */
    boolean save(String key, String value, Long time);

    /**
     * 返回字符串
     */
    String get(String key);
}
