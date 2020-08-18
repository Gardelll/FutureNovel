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

import java.util.Date;
import java.util.List;
import java.util.UUID;
import net.wlgzs.futurenovel.dao.ReadHistoryDao;
import net.wlgzs.futurenovel.exception.FutureNovelException;
import net.wlgzs.futurenovel.model.Account;
import net.wlgzs.futurenovel.model.ReadHistory;
import net.wlgzs.futurenovel.model.Section;
import org.springframework.dao.DataAccessException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReadHistoryService {

    private final ReadHistoryDao readHistoryDao;

    public ReadHistoryService(ReadHistoryDao readHistoryDao) {
        this.readHistoryDao = readHistoryDao;
    }

    @Transactional
    public void recordReadHistory(@NonNull Account account, @NonNull Section section) {
        var readHistory = new ReadHistory(UUID.randomUUID(), account.getUid(), section.getUniqueId(), new Date());
        try {
            int ret = readHistoryDao.insertReadHistory(readHistory);
            if (ret != 1)
                throw new FutureNovelException(FutureNovelException.Error.ITEM_NOT_FOUND, "逐步添加操作返回了不是 1 的值：" + ret);
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public void deleteReadHistory(@NonNull UUID historyId, @NonNull UUID accountId) {
        try {
            int ret = readHistoryDao.deleteReadHistory(historyId, accountId);
            if (ret != 1)
                throw new FutureNovelException(FutureNovelException.Error.ITEM_NOT_FOUND, "逐步删除操作返回了不是 1 的值：" + ret);
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public void clearReadHistory(@NonNull Account account, @Nullable Date after, @Nullable Date before) {
        try {
            int ret = readHistoryDao.deleteReadHistoryByAccountId(account.getUid(), after, before);
            if (ret == 0)
                throw new FutureNovelException(FutureNovelException.Error.ITEM_NOT_FOUND, "清空阅读历史失败");
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public long gc() {
        return readHistoryDao.historyGC();
    }

    public List<ReadHistory> getReadHistory(@NonNull Account account, @Nullable Date after, @Nullable Date before) {
        try {
            var result = readHistoryDao.getReadHistoryByAccountId(account.getUid(), after, before);
            if (result == null) return List.of();
            return result;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

}
