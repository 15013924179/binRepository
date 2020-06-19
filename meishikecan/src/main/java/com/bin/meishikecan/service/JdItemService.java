package com.bin.meishikecan.service;

import com.bin.meishikecan.entity.JdItem;

import java.util.List;
import java.util.Map;

public interface JdItemService {
    public List<JdItem> findByParam(Map map);

    public void save(JdItem jdItem);
}
