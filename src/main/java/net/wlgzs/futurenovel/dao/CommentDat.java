package net.wlgzs.futurenovel.dao;

import java.util.UUID;
import net.wlgzs.futurenovel.model.Comment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.springframework.dao.DataAccessException;

public interface CommentDat {

    @Insert({"INSERT INTO `comment` (`uniqueId`, `accountId`, `sectionId`, `rating`, `text`) ",
            "VALUES (#{uniqueId}, #{accountId}, #{sectionId}, #{rating}, #{text})"})
    int insertComment(Comment comment) throws DataAccessException;

    @Delete("DELETE FROM `comment` WHERE `comment`.`uniqueId` = #{uniqueId}")
    int deleteComment(@Param("uniqueId") UUID commentId) throws DataAccessException;



}
