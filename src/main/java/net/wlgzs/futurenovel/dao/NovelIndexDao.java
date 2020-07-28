package net.wlgzs.futurenovel.dao;

import com.fasterxml.jackson.databind.node.ArrayNode;
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

    @Update({"<script>",
            "UPDATE `novel_index` <set> ",
            "<if test='title != null'>`title` = #{title},</if> ",
            "<if test='copyright != null'>`copyright` = #{copyright},</if> ",
            "<if test='authors != null'>`authors` = #{authors},</if> ",
            "<if test='description != null'>`description` = #{description},</if> ",
            "<if test='tags != null'>`tags` = #{tags},</if> ",
            "<if test='series != null'>`series` = #{series},</if> ",
            "<if test='publisher != null'>`publisher` = #{publisher},</if> ",
            "<if test='pubdate != null'>`pubdate` = #{pubdate},</if> ",
            "<if test='coverImgUrl != null'>`coverImgUrl` = #{coverImgUrl},</if> ",
            "<if test='chapters != null'>`chapters` = #{chapters}</if> ",
            "</set> WHERE `novel_index`.`uniqueId` = #{uniqueId}",
            "</script>"})
    int updateNovelIndex(@Param("uniqueId") UUID novelId,
                         @Param("title") String title,
                         @Param("copyright") NovelIndex.Copyright copyright,
                         @Param("authors") String authors,
                         @Param("description") String description,
                         @Param("tags") String tags,
                         @Param("series") String series,
                         @Param("publisher") String publisher,
                         @Param("pubdate") Date pubdate,
                         @Param("coverImgUrl") String coverImgUrl,
                         @Param("chapters") ArrayNode chapters) throws DataAccessException;

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

    @Select("SELECT COUNT(*) FROM `novel_index`")
    long size();

    @Select({"<script>",
            "SELECT `uniqueId`, `uploader`, `title`, `copyright`, ",
            "`authors`, `description`, `rating`, `tags`, `series`, ",
            "`publisher`, `pubdate`, `hot`, `coverImgUrl`, `chapters` FROM `novel_index` ",
            "<if test='orderBy != null'>ORDER BY ${orderBy}</if> ",
            "LIMIT ${offset},${count}",
            "</script>"})
    List<NovelIndex> getAll(@Param("offset") int offset, @Param("count") int count, @Param("orderBy") String orderBy);

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
