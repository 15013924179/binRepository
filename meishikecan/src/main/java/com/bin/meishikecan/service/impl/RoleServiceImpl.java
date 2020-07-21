package com.bin.meishikecan.service.impl;

import com.bin.meishikecan.service.RoleService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.bin.meishikecan.dao.RoleMapper;
import com.bin.meishikecan.entity.Role;

@Service
public class RoleServiceImpl implements RoleService {

    @Resource
    private RoleMapper roleMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return roleMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(Role record) {
        return roleMapper.insert(record);
    }

    @Override
    public int insertSelective(Role record) {
        return roleMapper.insertSelective(record);
    }

    @Override
    public Role selectByPrimaryKey(Long id) {
        return roleMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(Role record) {
        return roleMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(Role record) {
        return roleMapper.updateByPrimaryKey(record);
    }

}
