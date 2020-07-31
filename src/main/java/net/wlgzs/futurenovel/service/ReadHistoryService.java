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
                throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "逐步添加操作返回了不是 1 的值：" + ret);
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public void deleteReadHistory(@NonNull UUID historyId, @NonNull UUID accountId) {
        try {
            int ret = readHistoryDao.deleteReadHistory(historyId, accountId);
            if (ret != 1)
                throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "逐步删除操作返回了不是 1 的值：" + ret);
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public void clearReadHistory(@NonNull Account account, @Nullable Date after, @Nullable Date before) {
        try {
            int ret = readHistoryDao.deleteReadHistoryByAccountId(account.getUid(), after, before);
            if (ret == 1)
                throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "清空阅读历史失败");
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
