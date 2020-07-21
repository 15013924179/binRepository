package com.bin.meishikecan.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
    * account
    */
@Data
public class Account implements Serializable {
    /**
    * id
    */
    private Long id;

    /**
    * 用户名
    */
    private String username;

    /**
    * 密码
    */
    private String password;

    /**
    * 手机号
    */
    private String phone;

    /**
    * 状态  0禁用  1启用
    */
    private Byte status;

    /**
    * 头像
    */
    private String avatar;

    /**
    * 邮箱
    */
    private String email;

    /**
    * 昵称
    */
    private String nickname;

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

    /**
    * 过期时间
    */
    private Date overTime;

    /**
    * 是否会过期  0 过期  1永久
    */
    private Byte isOver;

    /**
    * 最近登录时间
    */
    private Date loginTime;

    /**
    * 登录ip
    */
    private String loginIp;

    /**
    * 角色id
    */
    private Long roleId;

    /**
    * 密码盐
    */
    private String salt;


    public String getCredentialsSalt(){
        return username+salt;
    }
}