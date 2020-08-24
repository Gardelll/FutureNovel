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
import net.wlgzs.futurenovel.dao.CommentDao;
import net.wlgzs.futurenovel.exception.FutureNovelException;
import net.wlgzs.futurenovel.model.Comment;
import net.wlgzs.futurenovel.packet.Responses;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final CommentDao commentDao;
    private final MessageSource messageSource;

    public CommentService(CommentDao commentDao, MessageSource messageSource) {
        this.commentDao = commentDao;
        this.messageSource = messageSource;
    }

    @Transactional
    public void addComment(@NonNull UUID accountId,
                           @NonNull UUID sectionId,
                           @NonNull byte rating,
                           @NonNull String text) {
        var comment = new Comment(UUID.randomUUID(), accountId, sectionId, rating, text, new Date());
        try {
            int ret = commentDao.insertComment(comment);
            if (ret != 1)
                throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, messageSource.getMessage("database.insert_one_not_except", new Object[] {ret}, LocaleContextHolder.getLocale()));
            commentDao.updateRating(sectionId);
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public void deleteComment(@NonNull UUID commentId, @Nullable UUID accountId) {
        try {
            int ret = commentDao.deleteComment(commentId, accountId);
            if (ret != 1)
                throw new FutureNovelException(FutureNovelException.Error.ITEM_NOT_FOUND, messageSource.getMessage("database.delete_one_not_except", new Object[] {ret}, LocaleContextHolder.getLocale()));
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public void clearAccountComment(@NonNull UUID accountId) {
        try {
            commentDao.deleteCommentByAccountId(accountId);
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public void clearSectionComment(@NonNull UUID sectionId) {
        try {
            commentDao.deleteCommentBySectionId(sectionId);
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    public List<Responses.CommentInfo> getCommentsByAccount(@NonNull UUID accountId, @NonNull int offset, @NonNull int count) {
        try {
            var result = commentDao.getCommentInfoByAccountId(accountId, offset, count);
            if (result == null) return List.of();
            return result;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    public List<Responses.CommentInfo> getComments(UUID sectionId, @NonNull int offset, @NonNull int count) {
        try {
            var result = commentDao.getCommentInfoBySectionId(sectionId, offset, count);
            if (result == null) return List.of();
            return result;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    public List<Responses.CommentInfo> getComments(@NonNull int offset, @NonNull int count) {
        try {
            var result = commentDao.getCommentInfoForAdmin(offset, count);
            if (result == null) return List.of();
            return result;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    public Comment getComment(@NonNull UUID commentId) {
        try {
            var result = commentDao.getComment(commentId);
            if (result == null) throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION);
            return result;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public long gc() {
        return commentDao.commentGC();
    }

}
