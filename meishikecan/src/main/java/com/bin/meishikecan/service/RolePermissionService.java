package com.bin.meishikecan.service;

import com.bin.meishikecan.entity.RolePermission;
public interface RolePermissionService{


    int deleteByPrimaryKey(Long id);

    int insert(RolePermission record);

    int insertSelective(RolePermission record);

    RolePermission selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RolePermission record);

    int updateByPrimaryKey(RolePermission record);

}
