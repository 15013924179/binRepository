package com.bin.meishikecan.service.impl;

import com.bin.meishikecan.dao.JdItemDao;
import com.bin.meishikecan.entity.JdItem;
import com.bin.meishikecan.service.JdItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class JdItemServiceImpl implements JdItemService {

    @Autowired
    private JdItemDao jdItemDao;

    @Override
    public List<JdItem> findByParam(Map map) {
        return jdItemDao.findByParam(map);
    }

    @Override
    public void save(JdItem jdItem) {
        jdItemDao.save(jdItem);
    }
}
