package net.wlgzs.futurenovel.dao;

import java.util.List;
import java.util.UUID;
import net.wlgzs.futurenovel.bean.NovelChapter;
import net.wlgzs.futurenovel.model.Section;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.dao.DataAccessException;

public interface SectionDao {

    @Select("SELECT `uniqueId`, `fromChapter`, `title`, `text` FROM `section` WHERE `section`.`uniqueId` = #{uniqueId} LIMIT 1")
    Section getSectionById(@Param("uniqueId") UUID uniqueId) throws DataAccessException;

    @Insert({"INSERT INTO `section` (`uniqueId`, `fromChapter`, `title`, `text`) ",
            "VALUES (#{uniqueId}, #{fromChapter}, #{title}, #{text})"})
    int insertSection(Section section) throws DataAccessException;

    @Update({"<script>", "UPDATE `section` <set> ",
        "<if test='title != null'>`title` = #{title},</if> ",
        "<if test='text != null'>`text` = #{text}</if> ",
        "</set> WHERE `section`.`uniqueId` = #{uniqueId} </script>"})
    int updateSection(@Param("uniqueId") UUID sectionId,
                      @Param("title") String title,
                      @Param("text") String text) throws DataAccessException;

    @Select("SELECT `uniqueId`, `fromChapter`, `title` FROM `section` WHERE `section`.`fromChapter` = #{fromChapter}")
    List<NovelChapter.SectionInfo> getSectionInfoByFromChapter(@Param("fromChapter") UUID fromChapter) throws DataAccessException;

    @Select({"<script> ",
        "SELECT `uniqueId`, `fromChapter`, `title` FROM `section` WHERE `section`.`fromChapter` IN (",
        "<foreach collection='list' item='fromChapter' index='index' open=' ' separator=',' close=' '>",
        "#{fromChapter}",
        "</foreach>) </script>"
    })
    List<NovelChapter.SectionInfo> getSectionInfoByFromChapterList(List<UUID> fromChapterList) throws DataAccessException;

    @Delete("DELETE FROM `section` WHERE `section`.`uniqueId` = #{uniqueId}")
    int deleteSection(Section section) throws DataAccessException;

    @Delete("DELETE FROM `section` WHERE `section`.`uniqueId` = #{uniqueId}")
    int deleteSectionById(@Param("uniqueId") UUID uniqueId) throws DataAccessException;

    @Delete("DELETE FROM `section` WHERE `section`.`fromChapter` = #{fromChapter}")
    int deleteSectionByFromChapter(@Param("fromChapter") UUID fromChapter) throws DataAccessException;

    @Delete({"<script> ",
        "DELETE FROM `section` WHERE `section`.`fromChapter` IN (",
        "<foreach collection='list' item='fromChapter' index='index' open=' ' separator=',' close=' '>",
        "#{fromChapter}",
        "</foreach>) </script>"
    })
    int deleteSectionByFromChapterList(List<UUID> fromChapters) throws DataAccessException;

    @Select({"<script>",
            "SELECT `uniqueId`, `fromChapter`, `title`, `text` FROM `section` ",
            "WHERE `section`.`fromChapter` = #{fromChapter} ",
            "<if test='orderBy != null'>ORDER BY ${orderBy}</if> ",
            "LIMIT ${offset},${count}",
            "</script>"})
    List<Section> getSectionByFromChapter(@Param("fromChapter") UUID fromChapter, @Param("offset") int offset, @Param("count") int count, @Param("orderBy") String orderBy) throws DataAccessException;

    @Select({"SELECT COUNT(*) FROM `section` ",
            "WHERE `section`.`fromChapter` = #{fromChapter}"})
    int countSectionByFromChapter(@Param("fromChapter") UUID fromChapter) throws DataAccessException;

    // FullText Search
    @Select({"<script>",
            "SELECT `uniqueId`, `fromChapter`, `title`, `text` FROM `section` ",
            "WHERE MATCH (`title`, `text`) AGAINST (#{keywords} IN BOOLEAN MODE) ",
            "LIMIT ${offset},${count}",
            "</script>"})
    List<Section> getSectionByKeywords(@Param("keywords") String keywords, @Param("offset") int offset, @Param("count") int count) throws DataAccessException;

    @Select({"SELECT COUNT(*) FROM `section` ",
            "WHERE MATCH (`title`, `text`) AGAINST (#{keywords} IN BOOLEAN MODE) "})
    long countSectionByKeywords(@Param("keywords") String keywords) throws DataAccessException;

}
