package com.bin.meishikecan.service.impl;

import com.bin.meishikecan.service.RedisService;
import com.bin.meishikecan.utils.RedisUtils;
import com.bin.meishikecan.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RedisServiceImpl implements RedisService {

    @Autowired
    private RedisUtils redisUtils;


    /**
     * 设置单个值字符串,并设置过期时间,为null则不设置,单位是秒
     */
    @Override
    public boolean save(String key, String value, Long time) {
        if (time==null){
            return redisUtils.set(key,value);
        }else{
            return redisUtils.set(key,value,time);
        }
    }

    /**
     * 返回字符串
     */
    @Override
    public String get(String key) {
        return (String) redisUtils.get(key);
    }
}
