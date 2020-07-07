package net.wlgzs.futurenovel.dao;

import net.wlgzs.futurenovel.model.Account;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.UUID;

public interface AccountDao {
    @Select("SELECT * FROM `novel_db`.`accounts` WHERE `accounts`.`uid` = #{uid}")
    Account getAccount(@Param("uid") UUID uid);
}
