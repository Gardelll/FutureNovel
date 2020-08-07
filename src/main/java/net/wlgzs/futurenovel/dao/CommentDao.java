package net.wlgzs.futurenovel.dao;

import java.util.List;
import java.util.UUID;
import net.wlgzs.futurenovel.model.Comment;
import net.wlgzs.futurenovel.packet.Responses;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.dao.DataAccessException;

public interface CommentDao {

    @Insert({"INSERT INTO `comment` (`uniqueId`, `accountId`, `sectionId`, `rating`, `text`, `createTime`) ",
            "VALUES (#{uniqueId}, #{accountId}, #{sectionId}, #{rating}, #{text}, #{createTime})"})
    int insertComment(Comment comment) throws DataAccessException;
    
    @Update({
        "UPDATE `novel_index` ",
        "SET `novel_index`.`rating` = ",
        "        ( ",
        "            SELECT AVG(`comment`.`rating`) AS `aver` ",
        "            FROM `comment` ",
        "            WHERE `comment`.`sectionId` IN ( ",
        "                SELECT `section`.`uniqueId` ",
        "                FROM `section` ",
        "                WHERE `section`.`fromChapter` IN ( ",
        "                    SELECT `chapter`.`uniqueId` ",
        "                    FROM `chapter` ",
        "                    WHERE `chapter`.`fromNovel` = ( ",
        "                        SELECT `chapter`.`fromNovel` ",
        "                        FROM `chapter` ",
        "                        WHERE `chapter`.`uniqueId` = ( ",
        "                            SELECT `section`.`fromChapter` ",
        "                            FROM `section` ",
        "                            WHERE `section`.`uniqueId` = #{sectionId} ",
        "                        ) ",
        "                    ) ",
        "                ) ",
        "            ) ",
        "        ) ",
        "WHERE `novel_index`.`uniqueId` = ( ",
        "    SELECT `chapter`.`fromNovel` ",
        "    FROM `chapter` ",
        "    WHERE `chapter`.`uniqueId` = ( ",
        "        SELECT `section`.`fromChapter` ",
        "        FROM `section` ",
        "        WHERE `section`.`uniqueId` = #{sectionId} ",
        "    ) ",
        ")"
    })
    void updateRating(UUID sectionId) throws DataAccessException;

    @Delete({
        "<script> DELETE FROM `comment` <where> ",
        "`comment`.`uniqueId` = #{uniqueId} ",
        "<if test='accountId != null'>AND `comment`.`accountId` = #{accountId}</if> ",
        "</where> </script>"
    })
    int deleteComment(@Param("uniqueId") UUID commentId, @Param("accountId") UUID accountId) throws DataAccessException;

    @Delete("DELETE FROM `comment` WHERE `comment`.`sectionId` = #{sectionId}")
    int deleteCommentBySectionId(@Param("sectionId") UUID sectionId) throws DataAccessException;

    @Delete({
        "DELETE FROM `comment` WHERE `comment`.`sectionId` NOT IN ",
        "(SELECT `section`.`uniqueId` FROM `section`) ",
        "OR `comment`.`accountId` NOT IN (SELECT `accounts`.`uid` FROM `accounts`)",
    })
    long commentGC() throws DataAccessException;

    @Delete("DELETE FROM `comment` WHERE `comment`.`accountId` = #{accountId}")
    int deleteCommentByAccountId(@Param("accountId") UUID accountId) throws DataAccessException;

    @Select({
        "SELECT ",
        "    `comment`.`uniqueId`, ",
        "    `comment`.`accountId`, ",
        "    `comment`.`sectionId`, ",
        "    `comment`.`rating`, ",
        "    `comment`.`text`, ",
        "    `comment`.`createTime`, ",
        "    `accounts`.`userName`, ",
        "    `accounts`.`profileImgUrl`, ",
        "    FLOOR( ",
        "        0.5 *( ",
        "            SQRT( ",
        "                8 * `accounts`.`experience` / 100 + 1 ",
        "            ) -1 ",
        "        ) ",
        "    ) AS `level`, ",
        "    (SELECT COUNT(*) FROM `comment` WHERE `comment`.`accountId` = #{accountId}) AS `total` ",
        "FROM ",
        "    `comment`, `accounts` ",
        "WHERE ",
        "    `comment`.`accountId` = #{accountId} AND `accounts`.`uid` = #{accountId} ",
        "ORDER BY ",
        "    `comment`.`createTime` ",
        "DESC ",
        "LIMIT ${offset}, ${count}"
    })
    List<Responses.CommentInfo> getCommentInfoByAccountId(@Param("accountId") UUID accountId, @Param("offset") int offset, @Param("count") int count) throws DataAccessException;

    @Select({
        "SELECT ",
        "    `comment`.`uniqueId`, ",
        "    `comment`.`accountId`, ",
        "    `comment`.`sectionId`, ",
        "    `comment`.`rating`, ",
        "    `comment`.`text`, ",
        "    `comment`.`createTime`, ",
        "    `accounts`.`userName`, ",
        "    `accounts`.`profileImgUrl`, ",
        "    FLOOR( ",
        "        0.5 *( ",
        "            SQRT( ",
        "                8 * `accounts`.`experience` / 100 + 1 ",
        "            ) -1 ",
        "        ) ",
        "    ) AS `level`, ",
        "    (SELECT COUNT(*) FROM `comment` WHERE `comment`.`sectionId` = #{sectionId}) AS `total` ",
        "FROM ",
        "    `comment`, `accounts` ",
        "WHERE ",
        "    `comment`.`sectionId` = #{sectionId} AND `comment`.`accountId` = `accounts`.`uid` ",
        "ORDER BY ",
        "    `comment`.`createTime` ",
        "DESC ",
        "LIMIT ${offset}, ${count}"
    })
    List<Responses.CommentInfo> getCommentInfoBySectionId(@Param("sectionId") UUID sectionId, @Param("offset") int offset, @Param("count") int count) throws DataAccessException;

    @Select({
        "SELECT ",
        "    `comment`.`uniqueId`, ",
        "    `comment`.`accountId`, ",
        "    `comment`.`sectionId`, ",
        "    `comment`.`rating`, ",
        "    `comment`.`text`, ",
        "    `comment`.`createTime`, ",
        "    `accounts`.`userName`, ",
        "    `accounts`.`profileImgUrl`, ",
        "    FLOOR( ",
        "        0.5 *( ",
        "            SQRT( ",
        "                8 * `accounts`.`experience` / 100 + 1 ",
        "            ) -1 ",
        "        ) ",
        "    ) AS `level`, ",
        "    (SELECT COUNT(*) FROM `comment`) AS `total` ",
        "FROM ",
        "    `comment` ",
        "INNER JOIN `accounts` ON `comment`.`accountId` = `accounts`.`uid`",
        "ORDER BY ",
        "    `comment`.`createTime` ",
        "DESC ",
        "LIMIT ${offset}, ${count}"
    })
    List<Responses.CommentInfo> getCommentInfoForAdmin(@Param("offset") int offset, @Param("count") int count) throws DataAccessException;

    @Select({
        "SELECT `comment`.`uniqueId`, `comment`.`accountId`, `comment`.`sectionId`, `comment`.`rating`, `comment`.`text`, `comment`.`createTime` ",
        "FROM `comment` WHERE `comment`.`uniqueId` = #{uniqueId} LIMIT 1"
    })
    Comment getComment(@Param("uniqueId") UUID commentId) throws DataAccessException;

}
