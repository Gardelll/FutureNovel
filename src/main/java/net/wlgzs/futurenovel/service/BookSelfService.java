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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.List;
import java.util.UUID;
import net.wlgzs.futurenovel.dao.BookSelfDao;
import net.wlgzs.futurenovel.exception.FutureNovelException;
import net.wlgzs.futurenovel.model.Account;
import net.wlgzs.futurenovel.model.BookSelf;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookSelfService {

    private final BookSelfDao bookSelfDao;

    private final ObjectMapper objectMapper;

    private final MessageSource messageSource;

    public BookSelfService(BookSelfDao bookSelfDao, ObjectMapper objectMapper, MessageSource messageSource) {
        this.bookSelfDao = bookSelfDao;
        this.objectMapper = objectMapper;
        this.messageSource = messageSource;
    }

    @Transactional
    public BookSelf createBookSelf(@NonNull Account account, @NonNull String title) {
        try {
            var bookSelf = new BookSelf(UUID.randomUUID(), account.getUid(), title, new ArrayNode(objectMapper.getNodeFactory()));
            int ret = bookSelfDao.insertBookSelf(bookSelf);
            if (ret != 1)
                throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, messageSource.getMessage("database.insert_one_not_except", new Object[] {ret}, LocaleContextHolder.getLocale()));
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
                throw new FutureNovelException(FutureNovelException.Error.ITEM_NOT_FOUND, messageSource.getMessage("database.delete_one_not_except", new Object[] {ret}, LocaleContextHolder.getLocale()));
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public void clearAccountBookSelves(@NonNull UUID accountId) {
        try {
            bookSelfDao.deleteBookSelvesByAccountId(accountId);
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
                throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, messageSource.getMessage("database.update_one_not_except", new Object[] {ret}, LocaleContextHolder.getLocale()));
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    public BookSelf getBookSelf(@NonNull UUID bookSelfId) {
        try {
            var ret = bookSelfDao.getBookSelf(bookSelfId);
            if (ret == null) throw new FutureNovelException(FutureNovelException.Error.BOOK_SHELF_NOT_FOUND);
            return ret;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    public List<BookSelf> getBookSelves(@NonNull UUID accountId) {
        try {
            var ret = bookSelfDao.getBookSelvesByAccountId(accountId);
            if (ret == null) return List.of();
            return ret;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

}
