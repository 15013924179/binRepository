package com.bin.meishikecan.service.impl;

import com.bin.meishikecan.service.AccountService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.bin.meishikecan.entity.Account;
import com.bin.meishikecan.dao.AccountMapper;

@Service
public class AccountServiceImpl implements AccountService {

    @Resource
    private AccountMapper accountMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return accountMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(Account record) {
        return accountMapper.insert(record);
    }

    @Override
    public int insertSelective(Account record) {
        return accountMapper.insertSelective(record);
    }

    @Override
    public Account selectByPrimaryKey(Long id) {
        return accountMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(Account record) {
        return accountMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(Account record) {
        return accountMapper.updateByPrimaryKey(record);
    }

    @Override
    public Account selectByUsername(String username) {
        return accountMapper.selectByUsername(username);
    }

}
