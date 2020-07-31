package net.wlgzs.futurenovel.dao;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import net.wlgzs.futurenovel.model.ReadHistory;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.dao.DataAccessException;

public interface ReadHistoryDao {

    @Insert({"INSERT INTO `read_history` (`uniqueId`, `accountId`, `sectionId`, `createTime`) ",
            "VALUES (#{uniqueId}, #{accountId}, #{sectionId}, #{createTime})"})
    int insertReadHistory(ReadHistory readHistory) throws DataAccessException;

    @Delete("DELETE FROM `read_history` WHERE `read_history`.`uniqueId` = #{uniqueId} AND `read_history`.`accountId` = #{accountId}")
    int deleteReadHistory(@Param("uniqueId") UUID uniqueId, @Param("accountId") UUID accountId) throws DataAccessException;

    @Delete({
        "<script>",
        "DELETE FROM `read_history` ",
        "<where> `read_history`.`accountId` = #{accountId} ",
        "<if test='after != null'> AND `read_history`.`createTime` &gt; #{after} </if> ",
        "<if test='before != null'> AND `read_history`.`createTime` &lt; #{before} </if> ",
        "</where> </script>"
    })
    int deleteReadHistoryByAccountId(@Param("accountId") UUID accountId, @Param("after") Date after, @Param("before") Date before) throws DataAccessException;

    @Delete({
        "DELETE FROM `read_history` WHERE `read_history`.`sectionId` NOT IN ",
        "(SELECT `section`.`uniqueId` FROM `section`) ",
        "OR `read_history`.`accountId` NOT IN (SELECT `accounts`.`uid` FROM `accounts`)"
    })
    long historyGC() throws DataAccessException;

    @Select({
        "<script> SELECT ",
        "    `read_history`.`uniqueId`, ",
        "    `read_history`.`accountId`, ",
        "    `read_history`.`sectionId`, ",
        "    `read_history`.`createTime`, ",
        "    `section`.`title`, ",
        "    `novel_index`.`uniqueId` AS `novelIndexId`, ",
        "    `novel_index`.`title` AS `novelTitle`, ",
        "    `chapter`.`title` AS `chapterTitle`, ",
        "    `novel_index`.`coverImgUrl`, ",
        "    `novel_index`.`authors` ",
        "FROM ",
        "    `read_history`, ",
        "    `section`, ",
        "    `novel_index`, ",
        "    `chapter`",
        "<where> ",
        "    `read_history`.`accountId` = #{accountId} ",
        "    AND `section`.`uniqueId` = `read_history`.`sectionId` ",
        "    AND `novel_index`.`uniqueId` =( ",
        "        SELECT ",
        "            `chapter`.`fromNovel` ",
        "        FROM ",
        "            `chapter` ",
        "        WHERE ",
        "            `chapter`.`uniqueId` =( ",
        "            SELECT ",
        "                `section`.`fromChapter` ",
        "            FROM ",
        "                `section` ",
        "            WHERE ",
        "                `section`.`uniqueId` = `read_history`.`sectionId` ",
        "        ) ",
        "    )",
        "    AND `chapter`.`uniqueId` =( ",
        "        SELECT ",
        "            `section`.`fromChapter` ",
        "        FROM ",
        "            `section` ",
        "        WHERE ",
        "            `section`.`uniqueId` = `read_history`.`sectionId` ",
        "    ) ",
        "    <if test='after != null'> AND `read_history`.`createTime` &gt; #{after} </if> ",
        "    <if test='before != null'> AND `read_history`.`createTime` &lt; #{before} </if>",
        "</where> </script>"
    })
    List<ReadHistory> getReadHistoryByAccountId(@Param("accountId") UUID accountId, @Param("after") Date after, @Param("before") Date before) throws DataAccessException;

}
