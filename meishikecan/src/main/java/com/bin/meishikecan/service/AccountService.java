package com.bin.meishikecan.service;

import com.bin.meishikecan.entity.Account;
public interface AccountService{


    int deleteByPrimaryKey(Long id);

    int insert(Account record);

    int insertSelective(Account record);

    Account selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Account record);

    int updateByPrimaryKey(Account record);

    Account selectByUsername(String username);


}
