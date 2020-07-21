package com.bin.meishikecan.dao;
import org.apache.ibatis.annotations.Param;
import java.util.List;

import com.bin.meishikecan.entity.Account;

public interface AccountMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Account record);

    int insertSelective(Account record);

    Account selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Account record);

    int updateByPrimaryKey(Account record);

    Account selectByUsername(@Param("username")String username);


}