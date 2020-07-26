package net.wlgzs.futurenovel.dao;

import java.util.List;
import net.wlgzs.futurenovel.model.Token;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.dao.DataAccessException;

public interface TokenDao {
    @Select("SELECT * FROM `token_store`")
    List<Token> getAll() throws DataAccessException;

    @Delete("TRUNCATE `token_store`")
    void clear() throws DataAccessException;

    @Insert({"<script> INSERT INTO `token_store` ",
             "(`token`, `accountUid`, `lastUse`) VALUES",
             "<foreach collection='list' item='token' index='index' open=' ' separator=',' close=' '>",
             "(#{token.token}, #{token.accountUid}, #{token.lastUse})",
             "</foreach> </script>"})
    int insertAll(List<Token> tokenList);
}
