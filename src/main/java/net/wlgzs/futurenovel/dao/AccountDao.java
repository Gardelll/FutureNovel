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
    @Select("SELECT * FROM `novel_db`.`accounts` WHERE `accounts`.`uid` = #{uid}")
    Account getAccount(@Param("uid") UUID uid) throws DataAccessException;

    @Insert("INSERT INTO `novel_db`.`accounts` (`userName`, `uid`, " +
        " `userPass`, `email`, `phone`, `registerIP`, `lastLoginIP`," +
        " `registerDate`, `lastLoginDate`, `status`, `permission`," +
        " `experience`, `profileImgUrl`) values(#{userName}, #{uid}," +
        " #{userPass}, #{email}, #{phone}, #{registerIP}, #{lastLoginIP}," +
        " #{registerDate}, #{lastLoginDate}, #{status}, #{permission}," +
        " #{experience}, #{profileImgUrl})")
    int insertAccount(Account account) throws DataAccessException;

    @Select("SELECT * FROM `novel_db`.`accounts` WHERE `accounts`.`userName` = #{userName}")
    Account getAccountByUsername(@Param("userName") String userName) throws DataAccessException;

    @Select("SELECT * FROM `novel_db`.`accounts` WHERE `accounts`.`email` = #{email}")
    Account getAccountByEmail(@Param("email") String email) throws DataAccessException;

    @Select("SELECT COUNT(*) FROM `novel_db`.`accounts` WHERE `accounts`.`userName` = #{userName}")
    int getAccountSizeByUsername(@Param("userName") String name) throws DataAccessException;

    @Select("SELECT COUNT(*) FROM `novel_db`.`accounts` WHERE `accounts`.`email` = #{email}")
    int getAccountSizeByEmail(@Param("email") String email) throws DataAccessException;

    @Select("SELECT * FROM `novel_db`.`accounts` WHERE `accounts`.`userName` = #{userName} OR `accounts`.`email` = #{userName}")
    Account getAccountForLogin(@Param("userName") String userName) throws DataAccessException;

    @Update("UPDATE `novel_db`.`accounts` SET `accounts`.`profileImgUrl` = #{profileImgUrl} WHERE `accounts`.`uid` = #{uid}")
    int updateProfileImg(Account account) throws DataAccessException;

    @Update("UPDATE `novel_db`.`accounts` SET `profileImgUrl` = #{profileImgUrl}," +
        "`userName` = #{userName}," +
        "`userPass` = #{userPass}, `email` = #{email}," +
        "`phone` = #{phone}," +
        "`lastLoginIP` = #{lastLoginIP}," +
        "`lastLoginDate` = #{lastLoginDate}," +
        "`status` = #{status}, `permission` = #{permission}," +
        "`experience` = #{experience}, `profileImgUrl` = #{profileImgUrl}" +
        " WHERE `accounts`.`uid` = #{uid}")
    int updateAccount(Account account) throws DataAccessException;

    @Delete("DELETE `novel_db`.`accounts` WHERE `accounts`.`uid` = #{uid}")
    int deleteAccount(Account account) throws DataAccessException;

    @Update("UPDATE `novel_db`.`accounts` SET `experience` = #{experience} WHERE `accounts`.`uid` = #{uid}")
    int updateExperience(Account account);
}
