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
import net.wlgzs.futurenovel.model.BookSelf;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.dao.DataAccessException;

public interface BookSelfDao {

    @Insert({"INSERT INTO `book_self` (`uniqueId`, `accountId`, `title`, `novels`) ",
        "VALUES (#{uniqueId}, #{accountId}, #{title}, #{novels})"})
    int insertBookSelf(BookSelf bookSelf) throws DataAccessException;

    @Delete("DELETE FROM `book_self` WHERE `book_self`.`uniqueId` = #{bookSelfId}")
    int deleteBookSelf(@Param("bookSelfId") UUID bookSelfId) throws DataAccessException;

    @Delete("DELETE FROM `book_self` WHERE `book_self`.`accountId` = #{accountId}")
    int deleteBookSelvesByAccountId(@Param("accountId") UUID accountId) throws DataAccessException;

    @Delete("DELETE FROM `book_self` WHERE `book_self`.`accountId` NOT IN (SELECT `accounts`.`uid` FROM `accounts`)")
    long bookSelfGC() throws DataAccessException;

    @Update({
        "UPDATE `book_self` SET ",
        "`title` = #{title}, ",
        "`novels` = #{novels} ",
        "WHERE `book_self`.`uniqueId` = #{uniqueId}"
    })
    int editBookSelf(BookSelf bookSelf) throws DataAccessException;

    @Select("SELECT `uniqueId`, `accountId`, `title`, `novels` FROM `book_self` WHERE `book_self`.`uniqueId` = #{bookSelfId} LIMIT 1")
    BookSelf getBookSelf(@Param("bookSelfId") UUID bookSelfId) throws DataAccessException;

    @Select("SELECT `uniqueId`, `accountId`, `title`, `novels` FROM `book_self` WHERE `book_self`.`accountId` = #{accountId}")
    List<BookSelf> getBookSelvesByAccountId(@Param("accountId") UUID accountId) throws DataAccessException;

}
