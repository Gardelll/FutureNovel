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

    @Select({"SELECT `uniqueId`, `uploader`, `title`, `copyright`, ",
            "`authors`, `description`, `rating`, `tags`, `series`, ",
            "`publisher`, `pubdate`, `hot`, `coverImgUrl`, `chapters` FROM `novel_index` WHERE `novel_index`.`uniqueId` = #{uniqueId} LIMIT 1"})
    NovelIndex getNovelIndexById(@Param("uniqueId") UUID uniqueId) throws DataAccessException;

    @Insert({"INSERT INTO `novel_index` (`uniqueId`, `uploader`, `title`, ",
            "`copyright`, `authors`, `description`, `rating`, `tags`, `series`, ",
            "`publisher`, `pubdate`, `hot`, `coverImgUrl`, `chapters`) ",
            "VALUES (#{uniqueId} , #{uploader} , #{title}, #{copyright}, ",
            "#{authors}, #{description}, #{rating}, #{tags}, ",
            "#{series}, #{publisher}, #{pubdate}, #{hot}, #{coverImgUrl}, ",
            "#{chapters})"})
    int insertNovelIndex(NovelIndex novelIndex) throws DataAccessException;

    @Update({"UPDATE `novel_index` SET ",
            "`uploader` = #{uploader}, `title` = #{title}, ",
            "`copyright` = #{copyright}, ",
            "`authors` = #{authors}, `description` = #{description}, ",
            "`rating` = #{rating}, `tags` = #{tags}, ",
            "`series` = #{series}, `publisher` = #{publisher}, ",
            "`pubdate` = #{pubdate}, `hot` = #{hot}, ",
            "`coverImgUrl` = #{coverImgUrl}, ",
            "`chapters` = #{chapters} ",
            "WHERE `novel_index`.`uniqueId` = #{uniqueId}"})
    int updateNovelIndex(NovelIndex novelIndex) throws DataAccessException;

    @Update({"UPDATE `novel_index` SET ",
            "`uploader` = #{uploader} , `title` = #{title}, ",
            "`copyright` = #{copyright}, ",
            "`authors` = #{authors}, `description` = #{description}, ",
            "`rating` = #{rating}, `tags` = #{tags}, ",
            "`series` = #{series}, `publisher` = #{publisher}, ",
            "`pubdate` = #{pubdate}, `hot` = #{hot}, ",
            "`coverImgUrl` = #{coverImgUrl}, ",
            "WHERE `novel_index`.`uniqueId` = #{uniqueId}"})
    int updateNovelIndexExceptContent(NovelIndex novelIndex) throws DataAccessException;

    @Delete("DELETE FROM `novel_index` WHERE `novel_index`.`uniqueId` = #{uniqueId}")
    int deleteNovelIndex(NovelIndex novelIndex) throws DataAccessException;

    @Delete("DELETE FROM `novel_index` WHERE `novel_index`.`uniqueId` = #{uniqueId}")
    int deleteNovelIndexById(@Param("uniqueId") UUID uniqueId) throws DataAccessException;

    @Select({"SELECT `uniqueId`, `uploader`, `title`, `copyright`, ",
            "`authors`, `description`, `rating`, `tags`, `series`, ",
            "`publisher`, `pubdate`, `hot`, `coverImgUrl`, `chapters` FROM `novel_index` ",
            "WHERE `uniqueId` = (SELECT `chapter`.`fromNovel` FROM `chapter` ",
            "WHERE `chapter`.`uniqueId` = (SELECT `section`.`fromChapter` FROM `section` ",
            "WHERE `section`.`uniqueId` = #{uniqueId}))"})
    NovelIndex getNovelIndexBySectionId(@Param("uniqueId") UUID uniqueId) throws DataAccessException;

    @Select({"SELECT `uniqueId`, `uploader`, `title`, `copyright`, ",
            "`authors`, `description`, `rating`, `tags`, `series`, ",
            "`publisher`, `pubdate`, `hot`, `coverImgUrl`, `chapters` FROM `novel_index` ",
            "WHERE `uniqueId` = (SELECT `chapter`.`fromNovel` FROM `chapter` ",
            "WHERE `chapter`.`uniqueId` = #{uniqueId})"})
    NovelIndex getNovelIndexByChapterId(@Param("uniqueId") UUID uniqueId) throws DataAccessException;

    @Select({"<script> ",
            "SELECT `uniqueId`, `uploader`, `title`, `copyright`, ",
            "`authors`, `description`, `rating`, `tags`, `series`, ",
            "`publisher`, `pubdate`, `hot`, `coverImgUrl`, `chapters` FROM `novel_index` ",
            "WHERE `uniqueId` IN (SELECT `chapter`.`fromNovel` FROM `chapter` ",
            "WHERE `chapter`.`uniqueId` IN ",
            "<foreach collection='list' item='fromChapter' index='index' open='(' separator=',' close=')'>",
            "#{fromChapter}",
            "</foreach>) </script>"})
    List<NovelIndex> getNovelIndexByChapterIdList(List<UUID> chapterIds) throws DataAccessException;

    @Select({"<script>",
            "SELECT `uniqueId`, `uploader`, `title`, `copyright`, ",
            "`authors`, `description`, `rating`, `tags`, `series`, ",
            "`publisher`, `pubdate`, `hot`, `coverImgUrl`, `chapters` FROM `novel_index` ",
            "WHERE `novel_index`.`uploader` = #{uploader} ",
            "<if test='orderBy != null'>ORDER BY ${orderBy}</if> ",
            "LIMIT ${offset},${count}",
            "</script>"})
    List<NovelIndex> getNovelIndexByUploader(@Param("uploader") UUID uploader, @Param("offset") int offset, @Param("count") int count, @Param("orderBy") String orderBy) throws DataAccessException;

    @Select({"<script>",
            "SELECT COUNT(*) FROM `novel_index` ",
            "WHERE `novel_index`.`uploader` = #{uploader} ",
            "</script>"})
    long countNovelIndexByUploader(@Param("uploader") UUID uploader) throws DataAccessException;

    @Update({"UPDATE `novel_index` SET `hot` = ",
            "(SELECT `novel_index`.`hot` FROM `novel_index` ",
            "WHERE `novel_index`.`uniqueId` = #{uniqueId}) + 1 ",
            "WHERE `novel_index`.`uniqueId` = #{uniqueId}"})
    int hotAddOne(@Param("uniqueId") UUID uniqueId) throws DataAccessException;

    @Select({"<script>",
            "SELECT `uniqueId`, `uploader`, `title`, `copyright`, ",
            "`authors`, `description`, `rating`, `tags`, `series`, ",
            "`publisher`, `pubdate`, `hot`, `coverImgUrl`, `chapters` FROM `novel_index` ",
            "WHERE `novel_index`.`pubdate` BETWEEN #{after} AND #{before} ",
            "<if test='orderBy != null'>ORDER BY ${orderBy}</if> ",
            "LIMIT ${offset},${count}",
            "</script>"})
    List<NovelIndex> getNovelIndexByPubDate(@Param("after") Date after, @Param("before") Date before, @Param("offset") int offset, @Param("count") int count, @Param("orderBy") String orderBy) throws DataAccessException;

    @Select({"SELECT COUNT(*) FROM `novel_index` ",
            "WHERE `novel_index`.`pubdate` BETWEEN #{after} AND #{before} "})
    long countNovelIndexByPubDate(@Param("after") Date after, @Param("before") Date before) throws DataAccessException;

    @Select("SELECT `tags` FROM `novel_index` ORDER BY `hot` DESC LIMIT ${offset},${count}")
    List<String> getAllTags(@Param("offset") int offset, @Param("count") int count) throws DataAccessException;

    @Select("SELECT `series` FROM `novel_index` ORDER BY `hot` DESC LIMIT ${offset},${count}")
    List<String> getAllSeries(@Param("offset") int offset, @Param("count") int count) throws DataAccessException;

    // FullText Search
    @Select({"<script>",
            "SELECT `uniqueId`, `uploader`, `title`, `copyright`, ",
            "`authors`, `description`, `rating`, `tags`, `series`, ",
            "`publisher`, `pubdate`, `hot`, `coverImgUrl`, `chapters` FROM `novel_index` ",
            "WHERE MATCH (`title`, `authors`, `description`, `tags`, `series`, `publisher`) AGAINST (#{keywords} IN BOOLEAN MODE) ",
            "<if test='orderBy != null'>ORDER BY ${orderBy}</if> ",
            "LIMIT ${offset},${count}",
            "</script>"})
    List<NovelIndex> getNovelIndexByKeywords(@Param("keywords") String keywords, @Param("offset") int offset, @Param("count") int count, @Param("orderBy") String orderBy) throws DataAccessException;

    @Select({"SELECT COUNT(*) FROM `novel_index` ",
            "WHERE MATCH (`title`, `authors`, `description`, `tags`, `series`, `publisher`) AGAINST (#{keywords} IN BOOLEAN MODE) "})
    long countNovelIndexByKeywords(@Param("keywords") String keywords) throws DataAccessException;

}
