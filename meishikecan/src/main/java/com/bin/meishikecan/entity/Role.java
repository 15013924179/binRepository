package com.bin.meishikecan.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
    * role
    */
@Data
public class Role implements Serializable {
    /**
    * id
    */
    private Long id;

    /**
    * 角色名
    */
    private String name;

    /**
    * 备注
    */
    private String comment;

    /**
    * 创建时间
    */
    private Date createTime;

    /**
    * 更新时间
    */
    private Date updateTime;
}