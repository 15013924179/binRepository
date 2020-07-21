package com.bin.meishikecan.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
    * permission
    */
@Data
public class Permission implements Serializable {
    /**
    * id
    */
    private Long id;

    /**
    * 权限名
    */
    private String name;

    /**
    * 路径
    */
    private String path;

    /**
    * 创建时间
    */
    private Date createTime;

    /**
    * 更新时间
    */
    private Date updateTime;

    /**
    * 排序
    */
    private Integer sort;

    /**
    * 父级权限
    */
    private Long parentId;
}