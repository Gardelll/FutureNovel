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

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.dao.DataAccessException;

public interface SettingDao {

    @Select("INSERT INTO `settings` (`setting_key`, `setting_value`) VALUES (#{key}, #{value})")
    void add(@Param("key") String key, @Param("value") String value) throws DataAccessException;

    @Select("UPDATE `settings` SET `setting_value` = #{value} WHERE `setting_key` = #{key}")
    void update(String key, String value) throws DataAccessException;

    @Select("SELECT `setting_value` FROM `settings` WHERE `setting_key` = #{key}")
    String get(@Param("key") String key) throws DataAccessException;

    @Select("DELETE FROM `settings` WHERE `setting_key` = #{key}")
    int remove(String key) throws DataAccessException;

}
