package com.bin.meishikecan.service.impl;

import com.bin.meishikecan.dao.DemoDao;
import com.bin.meishikecan.entity.Demo;
import com.bin.meishikecan.service.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DemoServiceImpl implements DemoService {

    @Autowired
    public DemoDao demoDao;

    @Override
    public List<Demo> findAll() {
        return demoDao.findAll();
    }
}
