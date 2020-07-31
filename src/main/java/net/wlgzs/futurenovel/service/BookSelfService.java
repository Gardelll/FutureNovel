package net.wlgzs.futurenovel.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.List;
import java.util.UUID;
import net.wlgzs.futurenovel.AppConfig;
import net.wlgzs.futurenovel.dao.BookSelfDao;
import net.wlgzs.futurenovel.exception.FutureNovelException;
import net.wlgzs.futurenovel.model.Account;
import net.wlgzs.futurenovel.model.BookSelf;
import org.springframework.dao.DataAccessException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookSelfService {

    private final BookSelfDao bookSelfDao;

    public BookSelfService(BookSelfDao bookSelfDao) {
        this.bookSelfDao = bookSelfDao;
    }

    @Transactional
    public BookSelf createBookSelf(@NonNull Account account, @NonNull String title) {
        try {
            var bookSelf = new BookSelf(UUID.randomUUID(), account.getUid(), title, new ArrayNode(AppConfig.objectMapper.getNodeFactory()));
            int ret = bookSelfDao.insertBookSelf(bookSelf);
            if (ret != 1)
                throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "逐步添加操作返回了不是 1 的值：" + ret);
            return bookSelf;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public void deleteBookSelf(@NonNull UUID bookSelfId) {
        try {
            int ret = bookSelfDao.deleteBookSelf(bookSelfId);
            if (ret != 1)
                throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "逐步删除操作返回了不是 1 的值：" + ret);
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public void clearAccountBookSelves(@NonNull UUID accountId) {
        try {
            int ret = bookSelfDao.deleteBookSelvesByAccountId(accountId);
            if (ret == 0)
                throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "清理失败");
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    @Deprecated
    public long gc() {
        return bookSelfDao.bookSelfGC();
    }

    @Transactional
    public void editBookSelf(@NonNull BookSelf bookSelf) {
        try {
            int ret = bookSelfDao.editBookSelf(bookSelf);
            if (ret != 1)
                throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "逐步修改操作返回了不是 1 的值：" + ret);
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    public BookSelf getBookSelf(@NonNull UUID bookSelfId) {
        try {
            var ret = bookSelfDao.getBookSelf(bookSelfId);
            if (ret == null) throw new FutureNovelException(FutureNovelException.Error.BOOK_SELF_NOT_FOUND);
            return ret;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    public List<BookSelf> getBookSelves(@NonNull Account account) {
        try {
            var ret = bookSelfDao.getBookSelvesByAccountId(account.getUid());
            if (ret == null) return List.of();
            return ret;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

}
