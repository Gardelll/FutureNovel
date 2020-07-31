package net.wlgzs.futurenovel.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import net.wlgzs.futurenovel.dao.CommentDao;
import net.wlgzs.futurenovel.exception.FutureNovelException;
import net.wlgzs.futurenovel.model.Comment;
import net.wlgzs.futurenovel.packet.s2c.CommentInfo;
import org.springframework.dao.DataAccessException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final CommentDao commentDao;

    public CommentService(CommentDao commentDao) {
        this.commentDao = commentDao;
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
                throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "逐步添加操作返回了不是 1 的值：" + ret);
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
                throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "逐步删除操作返回了不是 1 的值：" + ret);
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public void clearAccountComment(@NonNull UUID accountId) {
        try {
            int ret = commentDao.deleteCommentByAccountId(accountId);
            if (ret == 0)
                throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "清空用户评论失败");
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public void clearSectionComment(@NonNull UUID sectionId) {
        try {
            int ret = commentDao.deleteCommentBySectionId(sectionId);
            if (ret == 0)
                throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "清空小节评论失败");
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    public List<CommentInfo> getCommentsByAccount(@NonNull UUID accountId, @NonNull int offset, @NonNull int count) {
        try {
            var result = commentDao.getCommentInfoByAccountId(accountId, offset, count);
            if (result == null) return List.of();
            return result;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    public List<CommentInfo> getComments(UUID sectionId, @NonNull int offset, @NonNull int count) {
        try {
            var result = commentDao.getCommentInfoBySectionId(sectionId, offset, count);
            if (result == null) return List.of();
            return result;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    public List<CommentInfo> getComments(@NonNull int offset, @NonNull int count) {
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