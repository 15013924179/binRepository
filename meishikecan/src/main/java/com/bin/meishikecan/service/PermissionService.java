package com.bin.meishikecan.service;

import com.bin.meishikecan.entity.Permission;

import java.util.List;

public interface PermissionService{


    int deleteByPrimaryKey(Long id);

    int insert(Permission record);

    int insertSelective(Permission record);

    Permission selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Permission record);

    int updateByPrimaryKey(Permission record);

    List<Permission> selectPermissionByRoleId (Long roleId);

}
