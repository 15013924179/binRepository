package com.bin.meishikecan.dao;

import com.bin.meishikecan.entity.JdItem;

import java.util.List;
import java.util.Map;

public interface JdItemDao {
    List<JdItem> findByParam(Map map);

    void save(JdItem jdItem);
}
