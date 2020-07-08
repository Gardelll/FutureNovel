package net.wlgzs.futurenovel.dao;

import net.wlgzs.futurenovel.model.Account;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.UUID;

public interface AccountDao {
    @Select("SELECT * FROM `novel_db`.`accounts` WHERE `accounts`.`uid` = #{uid}")
    Account getAccount(@Param("uid") UUID uid);

    @Insert("INSERT INTO `novel_db`.`accounts` (`userName`, `uid`, " +
            " `userPass`, `email`, `phone`, `registerIP`, `lastLoginIP`," +
            " `registerDate`, `lastLoginDate`, `status`, `permission`," +
            " `experience`, `profileImgUrl`) values(#{userName}, #{uid}," +
            " #{userPass}, #{email}, #{phone}, #{registerIP}, #{lastLoginIP}," +
            " #{registerDate}, #{lastLoginDate}, #{status}, #{permission}," +
            " #{experience}, #{profileImgUrl})")
    int insertAccount(Account account);

    @Select("SELECT * FROM `novel_db`.`accounts` WHERE `accounts`.`userName` = #{userName}")
    Account getAccountByUsername(@Param("userName") String userName);

    @Select("SELECT * FROM `novel_db`.`accounts` WHERE `accounts`.`userName` = #{userName} AND `accounts`.`userPass` = #{userPass}")
    Account getAccountByUsernameAndPassword(@Param("userName") String userName, @Param("userPass")  String userPass);

    @Update("UPDATE `novel_db`.`accounts` SET `accounts`.`profileImgUrl` = #{profileImgUrl} WHERE `accounts`.`uid` = #{uid}")
    int updateProfileImg(Account account);

    @Update("UPDATE `novel_db`.`accounts` SET `profileImgUrl` = #{profileImgUrl}," +
            "`userName` = #{userName}," +
            "`userPass` = #{userPass}, `email` = #{email}," +
            "`phone` = #{phone}," +
            "`lastLoginIP` = #{lastLoginIP}," +
            "`lastLoginDate` = #{lastLoginDate}," +
            "`status` = #{status}, `permission` = #{permission}," +
            "`experience` = #{experience}, `profileImgUrl` = #{profileImgUrl}" +
            " WHERE `accounts`.`uid` = #{uid}")
    int updateAccount(Account account);
}
