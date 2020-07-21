package com.bin.meishikecan.service.impl;

import com.bin.meishikecan.service.PermissionService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.bin.meishikecan.entity.Permission;
import com.bin.meishikecan.dao.PermissionMapper;

import java.util.List;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Resource
    private PermissionMapper permissionMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return permissionMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(Permission record) {
        return permissionMapper.insert(record);
    }

    @Override
    public int insertSelective(Permission record) {
        return permissionMapper.insertSelective(record);
    }

    @Override
    public Permission selectByPrimaryKey(Long id) {
        return permissionMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(Permission record) {
        return permissionMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(Permission record) {
        return permissionMapper.updateByPrimaryKey(record);
    }

    @Override
    public List<Permission> selectPermissionByRoleId(Long roleId) {
        return permissionMapper.selectPermissionByRoleId(roleId);
    }


}
