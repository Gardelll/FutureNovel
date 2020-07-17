package net.wlgzs.futurenovel.dao;

import java.util.UUID;
import net.wlgzs.futurenovel.model.Account;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.dao.DataAccessException;

public interface AccountDao {
    @Select("SELECT * FROM `accounts` WHERE `accounts`.`uid` = #{uid} LIMIT 1")
    Account getAccount(@Param("uid") UUID uid) throws DataAccessException;

    @Insert("INSERT INTO `accounts` (`userName`, `uid`, " +
        " `userPass`, `email`, `phone`, `registerIP`, `lastLoginIP`," +
        " `registerDate`, `lastLoginDate`, `status`, `isVIP`, `permission`," +
        " `experience`, `profileImgUrl`) values(#{userName}, #{uid}," +
        " #{userPass}, #{email}, #{phone}, #{registerIP}, #{lastLoginIP}," +
        " #{registerDate}, #{lastLoginDate}, #{status}, #{isVIP}, #{permission}," +
        " #{experience}, #{profileImgUrl})")
    int insertAccount(Account account) throws DataAccessException;

    @Select("SELECT * FROM `accounts` WHERE `accounts`.`userName` = #{userName} LIMIT 1")
    Account getAccountByUsername(@Param("userName") String userName) throws DataAccessException;

    @Select("SELECT * FROM `accounts` WHERE `accounts`.`email` = #{email} LIMIT 1")
    Account getAccountByEmail(@Param("email") String email) throws DataAccessException;

    @Select("SELECT COUNT(*) FROM `accounts` WHERE `accounts`.`userName` = #{userName}")
    int getAccountSizeByUsername(@Param("userName") String name) throws DataAccessException;

    @Select("SELECT COUNT(*) FROM `accounts` WHERE `accounts`.`email` = #{email}")
    int getAccountSizeByEmail(@Param("email") String email) throws DataAccessException;

    @Select("SELECT * FROM `accounts` WHERE `accounts`.`userName` = #{userName} OR `accounts`.`email` = #{userName} LIMIT = 1")
    Account getAccountForLogin(@Param("userName") String userName) throws DataAccessException;

    @Update("UPDATE `accounts` SET `accounts`.`profileImgUrl` = #{profileImgUrl} WHERE `accounts`.`uid` = #{uid}")
    int updateProfileImg(Account account) throws DataAccessException;

    @Update("UPDATE `accounts` SET `accounts`.`isVIP` = #{isVIP} WHERE `accounts`.`uid` = #{uid}")
    int updateIsVIP(Account account) throws DataAccessException;

    @Update("UPDATE `accounts` SET `profileImgUrl` = #{profileImgUrl}," +
        "`userName` = #{userName}," +
        "`userPass` = #{userPass}, `email` = #{email}," +
        "`phone` = #{phone}," +
        "`lastLoginIP` = #{lastLoginIP}," +
        "`lastLoginDate` = #{lastLoginDate}," +
        "`status` = #{status}, `isVIP` = #{isVIP}, `permission` = #{permission}," +
        "`experience` = #{experience}, `profileImgUrl` = #{profileImgUrl}" +
        " WHERE `accounts`.`uid` = #{uid}")
    int updateAccount(Account account) throws DataAccessException;

    @Delete("DELETE `accounts` WHERE `accounts`.`uid` = #{uid}")
    int deleteAccount(Account account) throws DataAccessException;

    @Update("UPDATE `accounts` SET `experience` = #{experience} WHERE `accounts`.`uid` = #{uid}")
    int updateExperience(Account account);
}
