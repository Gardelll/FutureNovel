package net.wlgzs.futurenovel.dao;

import java.util.List;
import java.util.UUID;
import net.wlgzs.futurenovel.packet.Requests;
import net.wlgzs.futurenovel.model.Account;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.dao.DataAccessException;
import org.springframework.lang.Nullable;

public interface AccountDao {

    @Select({"SELECT `userName`, `uid`, ",
            " `userPass`, `email`, `phone`, `registerIP`, `lastLoginIP`,",
            " `registerDate`, `lastLoginDate`, `status`, `isVIP`, `permission`,",
            " `experience`, `profileImgUrl` FROM `accounts` WHERE `accounts`.`uid` = #{uid} LIMIT 1 FOR UPDATE"})
    Account getAccount(@Param("uid") UUID uid) throws DataAccessException;

    @Insert({"INSERT INTO `accounts` (`userName`, `uid`, ",
            " `userPass`, `email`, `phone`, `registerIP`, `lastLoginIP`,",
            " `registerDate`, `lastLoginDate`, `status`, `isVIP`, `permission`,",
            " `experience`, `profileImgUrl`) values(#{userName}, #{uid},",
            " #{userPass}, #{email}, #{phone}, #{registerIP}, #{lastLoginIP},",
            " #{registerDate}, #{lastLoginDate}, #{status}, #{isVIP}, #{permission},",
            " #{experience}, #{profileImgUrl})"})
    int insertAccount(Account account) throws DataAccessException;

    @Select({"<script>",
            "SELECT COUNT(*) FROM `accounts`",
            "<where>",
            " `accounts`.`userName` = #{userName} ",
            " <if test='uid != null'>",
            "   AND `accounts`.`uid` != #{uid}",
            " </if>",
            "</where> ",
            "FOR UPDATE</script>"})
    int getAccountSizeByUsername(@Param("userName") String name, @Param("uid") @Nullable UUID uid) throws DataAccessException;

    @Select({"<script>",
            "SELECT COUNT(*) FROM `accounts`",
            "<where>",
            " `accounts`.`email` = #{email} ",
            " <if test='uid != null'>",
            "   AND `accounts`.`uid` != #{uid}",
            " </if> ",
            "</where> ",
            "FOR UPDATE </script>"})
    int getAccountSizeByEmail(@Param("email") String email, @Param("uid") @Nullable UUID uid) throws DataAccessException;

    @Select({"SELECT `userName`, `uid`, ",
            " `userPass`, `email`, `phone`, `registerIP`, `lastLoginIP`,",
            " `registerDate`, `lastLoginDate`, `status`, `isVIP`, `permission`,",
            " `experience`, `profileImgUrl` FROM `accounts` WHERE `accounts`.`userName` = #{userName} OR `accounts`.`email` = #{userName} LIMIT 1 FOR UPDATE"})
    Account getAccountForLogin(@Param("userName") String userName) throws DataAccessException;

    @Select({"<script>",
            "SELECT `userName`, `uid`, ",
            " `userPass`, `email`, `phone`, `registerIP`, `lastLoginIP`,",
            " `registerDate`, `lastLoginDate`, `status`, `isVIP`, `permission`,",
            " `experience`, `profileImgUrl` FROM `accounts`",
            "<if test='orderBy != null'>ORDER BY ${orderBy} </if> ",
            "LIMIT ${offset},${count} FOR UPDATE",
            "</script>"})
    List<Account> getAccountForAdmin(@Param("offset") long offset, @Param("count") int count, @Param("orderBy") String orderBy) throws DataAccessException;

    @Select({"SELECT COUNT(*) FROM `accounts`"})
    long size() throws DataAccessException;

    @Update("UPDATE `accounts` SET `accounts`.`profileImgUrl` = #{profileImgUrl} WHERE `accounts`.`uid` = #{uid}")
    int updateProfileImg(Account account) throws DataAccessException;

    @Update("UPDATE `accounts` SET `accounts`.`isVIP` = #{isVIP} WHERE `accounts`.`uid` = #{uid}")
    int updateIsVIP(Account account) throws DataAccessException;

    @Update({"UPDATE `accounts` SET `profileImgUrl` = #{profileImgUrl},",
            "`userName` = #{userName},",
            "`userPass` = #{userPass}, `email` = #{email},",
            "`phone` = #{phone},",
            "`lastLoginIP` = #{lastLoginIP},",
            "`lastLoginDate` = #{lastLoginDate},",
            "`status` = #{status}, `isVIP` = #{isVIP}, `permission` = #{permission},",
            "`experience` = #{experience}, `profileImgUrl` = #{profileImgUrl}",
            " WHERE `accounts`.`uid` = #{uid}"})
    int updateAccount(Account account) throws DataAccessException;

    @Update({"<script>",
            "UPDATE `accounts` ",
            "<set> ",
            "<if test='userName != null'>userName=#{userName},</if> ",
            "<if test='password != null'>userPass=#{password},</if> ",
            "<if test='email != null'>email=#{email},</if> ",
            "<if test='phone != null'>phone=#{phone},</if> ",
            "<if test='profileImgUrl != null'>profileImgUrl=#{profileImgUrl},</if> ",
            "<if test='status != null'>status=#{status},</if> ",
            "<if test='vip != null'>isVip=#{vip},</if> ",
            "<if test='permission != null'>permission=#{permission}</if> ",
            "</set> ",
            "WHERE `accounts`.`uid` = #{uid}",
            "</script>"})
    int editAccount(Requests.EditAccountRequest account) throws DataAccessException;

    @Delete("DELETE FROM `accounts` WHERE `accounts`.`uid` = #{uid}")
    int deleteAccount(Account account) throws DataAccessException;

    @Update("UPDATE `accounts` SET `experience` = #{experience} WHERE `accounts`.`uid` = #{uid}")
    int updateExperience(Account account);

}
