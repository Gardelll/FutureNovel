/*
 *  Copyright (C) 2020 Future Studio
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.wlgzs.futurenovel.dao;

import java.util.List;
import java.util.UUID;
import net.wlgzs.futurenovel.model.Chapter;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.dao.DataAccessException;

public interface ChapterDao {

    @Select("SELECT `uniqueId`, `fromNovel`, `title`, `sections` FROM `chapter` WHERE `chapter`.`uniqueId` = #{uniqueId} LIMIT 1")
    Chapter getChapterById(@Param("uniqueId") UUID uniqueId) throws DataAccessException;

    @Insert({"INSERT INTO `chapter` (`uniqueId`, `fromNovel`, `title`, `sections`) ",
            "VALUES (#{uniqueId}, #{fromNovel}, #{title}, #{sections})"})
    int insertChapter(Chapter chapter) throws DataAccessException;

    @Update({"UPDATE `chapter` SET `fromNovel` = #{fromNovel}, `title` = #{title}, `sections` = #{sections} WHERE `chapter`.`uniqueId` = #{uniqueId}"})
    int updateChapter(Chapter chapter) throws DataAccessException;

    @Update({"UPDATE `chapter` SET `title` = #{title} WHERE `chapter`.`uniqueId` = #{uniqueId}"})
    int updateChapterTitle(Chapter chapter) throws DataAccessException;

    @Delete("DELETE FROM `chapter` WHERE `chapter`.`uniqueId` = #{uniqueId}")
    int deleteChapter(Chapter chapter) throws DataAccessException;

    @Delete("DELETE FROM `chapter` WHERE `chapter`.`uniqueId` = #{uniqueId}")
    int deleteChapterById(@Param("uniqueId") UUID uniqueId) throws DataAccessException;

    @Delete("DELETE FROM `chapter` WHERE `chapter`.`fromNovel` = #{fromNovel}")
    int deleteChapterByFromNovel(@Param("fromNovel") UUID fromNovel) throws DataAccessException;

    @Delete({
        "DELETE FROM `chapter` WHERE `chapter`.`fromNovel` NOT IN ",
        "(SELECT `novel_index`.`uniqueId` FROM `novel_index`) "
    })
    long chapterGC() throws DataAccessException;

    @Select({"<script>",
            "SELECT `uniqueId`, `fromNovel`, `title`, `sections` FROM `chapter` ",
            "WHERE `chapter`.`fromNovel` = #{fromNovel} ",
            "<if test='orderBy != null'>ORDER BY ${orderBy}</if> ",
            "LIMIT ${offset},${count}",
            "</script>"})
    List<Chapter> getChapterByFromNovel(@Param("fromNovel") UUID fromNovel, @Param("offset") int offset, @Param("count") int count, @Param("orderBy") String orderBy) throws DataAccessException;

    @Select({"SELECT COUNT(*) FROM `chapter` ",
            "WHERE `chapter`.`fromNovel` = #{fromNovel} "})
    int countChapterByFromNovel(@Param("fromNovel") UUID fromNovel) throws DataAccessException;

    @Select({"<script>",
            "SELECT `uniqueId`, `fromNovel`, `title`, `sections` FROM `chapter` ",
            "WHERE `chapter`.`title` = #{title} ",
            "<if test='orderBy != null'>ORDER BY ${orderBy}</if> ",
            "LIMIT ${offset},${count}",
            "</script>"})
    List<Chapter> getChapterByTitle(@Param("title") String title, @Param("offset") int offset, @Param("count") int count, @Param("orderBy") String orderBy) throws DataAccessException;

}
