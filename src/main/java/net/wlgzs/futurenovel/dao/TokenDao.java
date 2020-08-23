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
