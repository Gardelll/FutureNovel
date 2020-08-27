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

package net.wlgzs.futurenovel.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.wlgzs.futurenovel.dao.SettingDao;
import net.wlgzs.futurenovel.exception.FutureNovelException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class SettingService {

    private final SettingDao settingDao;

    private final ObjectMapper objectMapper;

    public SettingService(SettingDao settingDao,
                          ObjectMapper objectMapper) {
        this.settingDao = settingDao;
        this.objectMapper = objectMapper;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T get(@NonNull String key, @NonNull Class<T> tClass) {
        String value = settingDao.get(key);
        if (value == null) return null;
        if (String.class.equals(tClass)) {
            return (T) value;
        }
        try {
            return objectMapper.readValue(value, tClass);
        } catch (JsonProcessingException e) {
            log.error("无法反序列化 JSON", e);
            return null;
        }
    }

    @Nullable
    @Transactional
    @SuppressWarnings("unchecked")
    public <T> T put(@NonNull String key, @NonNull T value) {
        Object oldValue = get(key, value.getClass());
        if (oldValue == null) {
            if (value instanceof String) {
                settingDao.add(key, (String) value);
            } else {
                try {
                    settingDao.add(key, objectMapper.writeValueAsString(value));
                } catch (JsonProcessingException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        } else {
            if (value instanceof String) {
                settingDao.update(key, (String) value);
            } else {
                try {
                    settingDao.update(key, objectMapper.writeValueAsString(value));
                } catch (JsonProcessingException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }
        if (oldValue != null && value.getClass().equals(oldValue.getClass())) {
            return (T) oldValue;
        } else {
            return null;
        }
    }

    @Nullable
    @Transactional
    public <T> T remove(@NonNull String key, @NonNull Class<T> tClass) {
        T oldValue = get(key, tClass);
        if (0 == settingDao.remove(key)) throw new FutureNovelException(FutureNovelException.Error.ITEM_NOT_FOUND);
        if (oldValue != null && tClass.equals(oldValue.getClass())) {
            return oldValue;
        } else {
            return null;
        }
    }

}
