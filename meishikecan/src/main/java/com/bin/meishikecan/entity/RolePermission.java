package com.bin.meishikecan.entity;

import java.util.Date;
import lombok.Data;

/**
    * role_permission
    */
@Data
public class RolePermission {
    /**
    * id
    */
    private Long id;

    /**
    * 角色id
    */
    private Long roleId;

    /**
    * 权限id
    */
    private Long permissionId;

    /**
    * 创建时间
    */
    private Date createTime;
}