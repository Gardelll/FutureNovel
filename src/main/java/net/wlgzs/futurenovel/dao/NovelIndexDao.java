package net.wlgzs.futurenovel.dao;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import net.wlgzs.futurenovel.model.NovelIndex;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.dao.DataAccessException;

public interface NovelIndexDao {

    @Select({"SELECT `uniqueId`, `uploader`, `title`, ",
            "`authors`, `description`, `rating`, `tags`, `series`, ",
            "`publisher`, `pubdate`, `coverImgUrl`, `chapters` FROM `novel_index` WHERE `novel_index`.`uniqueId` = #{uniqueId} LIMIT 1"})
    NovelIndex getNovelIndexById(@Param("uniqueId") UUID uniqueId) throws DataAccessException;

    @Insert({"INSERT INTO `novel_index` (`uniqueId`, `uploader`, `title`, ",
            "`authors`, `description`, `rating`, `tags`, `series`, ",
            "`publisher`, `pubdate`, `coverImgUrl`, `chapters`) ",
            "VALUES (#{uniqueId} , #{uploader} , #{title}, ",
            "#{authors}, #{description}, #{rating}, #{tags}, ",
            "#{series}, #{publisher}, #{pubdate}, #{coverImgUrl}, ",
            "#{chapters})"})
    int insertNovelIndex(NovelIndex novelIndex) throws DataAccessException;

    @Update({"UPDATE `novel_index` SET ",
            "`uploader` = #{uploader} , `title` = #{title}, ",
            "`authors` = #{authors}, `description` = #{description}, ",
            "`rating` = #{rating}, `tags` = #{tags}, ",
            "`series` = #{series}, `publisher` = #{publisher}, ",
            "`pubdate` = #{pubdate}, `coverImgUrl` = #{coverImgUrl}, ",
            "`chapters` = #{chapters} ",
            "WHERE `novel_index`.`uniqueId` = #{uniqueId}"})
    int updateNovelIndex(NovelIndex novelIndex) throws DataAccessException;

    @Update({"UPDATE `novel_index` SET ",
            "`uploader` = #{uploader} , `title` = #{title}, ",
            "`authors` = #{authors}, `description` = #{description}, ",
            "`rating` = #{rating}, `tags` = #{tags}, ",
            "`series` = #{series}, `publisher` = #{publisher}, ",
            "`pubdate` = #{pubdate}, `coverImgUrl` = #{coverImgUrl} ",
            "WHERE `novel_index`.`uniqueId` = #{uniqueId}"})
    int updateNovelIndexExceptContent(NovelIndex novelIndex) throws DataAccessException;

    @Delete("DELETE `novel_index` WHERE `novel_index`.`uniqueId` = #{uniqueId}")
    int deleteNovelIndex(NovelIndex novelIndex) throws DataAccessException;

    @Delete("DELETE `novel_index` WHERE `novel_index`.`uniqueId` = #{uniqueId}")
    int deleteNovelIndexById(@Param("uniqueId") UUID uniqueId) throws DataAccessException;

    @Select({"<script>",
            "SELECT `uniqueId`, `uploader`, `title`, ",
            "`authors`, `description`, `rating`, `tags`, `series`, ",
            "`publisher`, `pubdate`, `coverImgUrl`, `chapters` FROM `novel_index` ",
            "WHERE `novel_index`.`uploader` = #{uploader} ",
            "<if test='orderBy != null'>ORDER BY ${orderBy}</if> ",
            "LIMIT ${offset},${count}",
            "</script>"})
    List<NovelIndex> getNovelIndexByUploader(@Param("uploader") UUID uploader, @Param("offset") int offset, @Param("count") int count, @Param("orderBy") String orderBy) throws DataAccessException;

    @Select({"<script>",
            "SELECT COUNT(*) FROM `novel_index` ",
            "WHERE `novel_index`.`uploader` = #{uploader} ",
            "</script>"})
    int countNovelIndexByUploader(@Param("uploader") UUID uploader) throws DataAccessException;

    @Select({"<script>",
            "SELECT `uniqueId`, `uploader`, `title`, ",
            "`authors`, `description`, `rating`, `tags`, `series`, ",
            "`publisher`, `pubdate`, `coverImgUrl`, `chapters` FROM `novel_index` ",
            "WHERE `novel_index`.`pubdate` BETWEEN #{after} AND #{before} ",
            "<if test='orderBy != null'>ORDER BY ${orderBy}</if> ",
            "LIMIT ${offset},${count}",
            "</script>"})
    List<NovelIndex> getNovelIndexByPubDate(@Param("after") Date after, @Param("before") Date before, @Param("offset") int offset, @Param("count") int count, @Param("orderBy") String orderBy) throws DataAccessException;

    @Select("SELECT `tags` FROM `novel_index` LIMIT ${offset},${count}")
    List<String> getAllTags(@Param("offset") int offset, @Param("count") int count) throws DataAccessException;

    @Select("SELECT `series` FROM `novel_index` LIMIT ${offset},${count}")
    List<String> getAllSeries(@Param("offset") int offset, @Param("count") int count) throws DataAccessException;

    // FullText Search
    @Select({"<script>",
            "SELECT `uniqueId`, `uploader`, `title`, ",
            "`authors`, `description`, `rating`, `tags`, `series`, ",
            "`publisher`, `pubdate`, `coverImgUrl`, `chapters` FROM `novel_index` ",
            "WHERE MATCH (`title`, `authors`, `description`, `tags`, `series`, `publisher`) AGAINST (#{keywords} IN BOOLEAN MODE) ",
            "<if test='orderBy != null'>ORDER BY ${orderBy}</if> ",
            "LIMIT ${offset},${count}",
            "</script>"})
    List<NovelIndex> getNovelIndexByKeywords(@Param("keywords") String keywords, @Param("offset") int offset, @Param("count") int count, @Param("orderBy") String orderBy) throws DataAccessException;

}
