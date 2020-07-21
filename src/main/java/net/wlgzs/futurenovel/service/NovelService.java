package net.wlgzs.futurenovel.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.wlgzs.futurenovel.dao.ChapterDao;
import net.wlgzs.futurenovel.dao.NovelIndexDao;
import net.wlgzs.futurenovel.dao.SectionDao;
import net.wlgzs.futurenovel.exception.FutureNovelException;
import net.wlgzs.futurenovel.model.Account;
import net.wlgzs.futurenovel.model.Chapter;
import net.wlgzs.futurenovel.model.NovelIndex;
import net.wlgzs.futurenovel.model.Section;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.dao.DataAccessException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 小说的增删改查
 */
@Service
@Slf4j
public class NovelService implements DisposableBean {

    private final NovelIndexDao novelIndexDao;

    private final ChapterDao chapterDao;

    private final SectionDao sectionDao;

    @Getter
    private final HashSet<String> tags = new HashSet<>();

    @Getter
    private final HashSet<String> series = new HashSet<>();

    private final ScheduledExecutorService executor;

    private final ScheduledFuture<?> future;

    public NovelService(NovelIndexDao novelIndexDao, ChapterDao chapterDao, SectionDao sectionDao) {
        this.novelIndexDao = novelIndexDao;
        this.chapterDao = chapterDao;
        this.sectionDao = sectionDao;
        executor = Executors.newScheduledThreadPool(1);
        future = executor.scheduleAtFixedRate(() -> {
            synchronized (executor) {
                if (executor.isShutdown()) return;
                final int count = 100;
                int i = 0;
                List<String> result;
                do {
                    result = novelIndexDao.getAllTags(i, count);
                    i += count;
                    result.forEach(s -> tags.addAll(Arrays.asList(s.split(","))));
                } while (result.size() >= count);
                i = 0;
                do {
                    result = novelIndexDao.getAllSeries(i, count);
                    i += count;
                    series.addAll(result);
                } while (result.size() >= count);
            }
        }, 1, 10, TimeUnit.MINUTES);
    }

    @NonNull
    @Transactional
    public NovelIndex createNovelIndex(@NonNull Account account,
                                       @NonNull NovelIndex.Copyright copyright,
                                       @NonNull String title,
                                       @NonNull List<String> authors,
                                       @NonNull String description,
                                       @NonNull byte rating,
                                       @NonNull List<String> tags,
                                       @NonNull String series,
                                       @NonNull String publisher,
                                       @NonNull Date pubdate,
                                       @Nullable String coverImgUrl) {
        switch (copyright) {
            case REPRINT:
            case NO_COPYRIGHT:
                account.checkPermission(Account.Permission.USER, Account.Permission.AUTHOR, Account.Permission.ADMIN);
                break;
            case FAN_FICTION:
            case ORIGINAL:
                account.checkPermission(Account.Permission.AUTHOR, Account.Permission.ADMIN);
                break;
        }
        try {
            var novelIndex = new NovelIndex(UUID.randomUUID(),
                    account.getUid(),
                    copyright,
                    title,
                    String.join(",", authors),
                    description,
                    rating,
                    String.join(",", tags),
                    series,
                    publisher,
                    pubdate,
                    coverImgUrl,
                    new ArrayNode(JsonNodeFactory.instance));
            var ret = novelIndexDao.insertNovelIndex(novelIndex);
            if (ret != 1) throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "逐步添加操作返回了不是 1 的值：" + ret);
            return novelIndex;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @NonNull
    @Transactional
    public Chapter addChapter(@NonNull Account account,
                              @NonNull NovelIndex novelIndex,
                              @Nullable String title) {
        if (novelIndex.getUploader().equals(account.getUid())) account.checkPermission();
        else account.checkPermission(Account.Permission.ADMIN);
        try {
            if (title == null) title = String.format("第 %d 章", chapterDao.countChapterByFromNovel(novelIndex.getUniqueId()) + 1);
            var chapter = new Chapter(UUID.randomUUID(), novelIndex.getUniqueId(), title, new ArrayNode(JsonNodeFactory.instance));
            var arrayNode = novelIndex.getChapters();
            arrayNode.add(chapter.getUniqueId().toString());
            var ret = chapterDao.insertChapter(chapter);
            if (ret != 1) throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "逐步添加操作返回了不是 1 的值：" + ret);
            ret = novelIndexDao.updateNovelIndex(novelIndex);
            if (ret != 1) throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "更新添加操作返回了不是 1 的值：" + ret);
            return chapter;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @NonNull
    @Transactional
    public Section addSection(@NonNull Account account,
                              @NonNull Chapter chapter,
                              @Nullable String title,
                              @NonNull String text) {
        try {
            var novelIndex = novelIndexDao.getNovelIndexById(chapter.getFromNovel());
            if (account.getUid().equals(novelIndex.getUploader())) account.checkPermission();
            else account.checkPermission(Account.Permission.ADMIN);
            if (title == null) title = String.format("第 %d 节", sectionDao.countSectionByFromChapter(chapter.getUniqueId()) + 1);
            var section = new Section(UUID.randomUUID(), chapter.getUniqueId(), title, text);
            var arrayNode = chapter.getSections();
            arrayNode.add(section.getUniqueId().toString());
            var ret = sectionDao.insertSection(section);
            if (ret != 1) throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "逐步添加操作返回了不是 1 的值：" + ret);
            ret = chapterDao.updateChapter(chapter);
            if (ret != 1) throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "更新操作返回了不是 1 的值：" + ret);
            return section;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public void deleteNovelIndex(@NonNull Account account, @NonNull NovelIndex novelIndex) {
        try {
            if (account.getUid().equals(novelIndex.getUploader())) account.checkPermission();
            else account.checkPermission(Account.Permission.ADMIN);
            var arrayNode = novelIndex.getChapters();
            for (var node : arrayNode) {
                UUID chapterId = UUID.fromString(node.asText());
                Chapter chapter = chapterDao.getChapterById(chapterId);
                deleteChapter(account, chapter, novelIndex);
            }
            var ret = novelIndexDao.deleteNovelIndex(novelIndex);
            if (ret != 1) throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "逐步删除操作返回了不是 1 的值：" + ret);
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public void deleteChapter(@NonNull Account account, @NonNull Chapter chapter, @Nullable NovelIndex novelIndex) {
        try {
            if (novelIndex == null) novelIndex = novelIndexDao.getNovelIndexById(chapter.getFromNovel());
            if (account.getUid().equals(novelIndex.getUploader())) account.checkPermission();
            else account.checkPermission(Account.Permission.ADMIN);
            var arrayNode = chapter.getSections();
            for (var node : arrayNode) {
                UUID sectionId = UUID.fromString(node.asText());
                Section section = sectionDao.getSectionById(sectionId);
                deleteSection(account, section, novelIndex);
            }
            var ret = chapterDao.deleteChapter(chapter);
            if (ret != 1) throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "逐步删除操作返回了不是 1 的值：" + ret);
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public void deleteSection(@NonNull Account account, @NonNull Section section, @Nullable NovelIndex novelIndex) {
        try {
            if (novelIndex == null) novelIndex = novelIndexDao.getNovelIndexById(chapterDao.getChapterById(section.getFromChapter()).getFromNovel());
            if (account.getUid().equals(novelIndex.getUploader())) account.checkPermission();
            else account.checkPermission(Account.Permission.ADMIN);
            var ret = sectionDao.deleteSection(section);
            if (ret != 1) throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "逐步删除操作返回了不是 1 的值：" + ret);
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public void editNovelIndex(@NonNull Account account, @NonNull NovelIndex novelIndex) {
        try {
            if (account.getUid().equals(novelIndex.getUploader())) account.checkPermission();
            else account.checkPermission(Account.Permission.ADMIN);
            var ret = novelIndexDao.updateNovelIndex(novelIndex);
            if (ret != 1) throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "逐步删除操作返回了不是 1 的值：" + ret);
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public void editChapter(@NonNull Account account, @NonNull Chapter chapter, @Nullable NovelIndex novelIndex) {
        try {
            if (novelIndex == null) novelIndex = novelIndexDao.getNovelIndexById(chapter.getFromNovel());
            if (account.getUid().equals(novelIndex.getUploader())) account.checkPermission();
            else account.checkPermission(Account.Permission.ADMIN);
            var ret = chapterDao.updateChapter(chapter);
            if (ret != 1) throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "逐步删除操作返回了不是 1 的值：" + ret);
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public void editSection(@NonNull Account account, @NonNull Section section, @Nullable NovelIndex novelIndex) {
        try {
            if (novelIndex == null) novelIndex = novelIndexDao.getNovelIndexById(chapterDao.getChapterById(section.getFromChapter()).getFromNovel());
            if (account.getUid().equals(novelIndex.getUploader())) account.checkPermission();
            else account.checkPermission(Account.Permission.ADMIN);
            var ret = sectionDao.updateSection(section);
            if (ret != 1) throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "逐步删除操作返回了不是 1 的值：" + ret);
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @NonNull
    public List<NovelIndex> findNovelIndexByUploader(@NonNull UUID uploader, @NonNull int offset, @NonNull int count, @NonNull String orderBy) {
        try {
            List<NovelIndex> list = novelIndexDao.getNovelIndexByUploader(uploader, offset, count, orderBy);
            return list == null ? List.of() : list;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @NonNull
    public List<NovelIndex> findNovelIndexByPubDate(@NonNull Date after, @NonNull Date before, @NonNull int offset, @NonNull int count, @NonNull String orderBy) {
        try {
            List<NovelIndex> list = novelIndexDao.getNovelIndexByPubDate(after, before, offset, count, orderBy);
            return list == null ? List.of() : list;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @NonNull
    public List<NovelIndex> searchNovelIndex(@NonNull String keywords, @NonNull int offset, @NonNull int count, @NonNull String orderBy) {
        try {
            List<NovelIndex> list = novelIndexDao.getNovelIndexByKeywords(keywords, offset, count, orderBy);
            return list == null ? List.of() : list;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @NonNull
    public List<Chapter> findChapterByFromNovel(@NonNull UUID fromNovel, @NonNull int offset, @NonNull int count, @NonNull String orderBy) {
        try {
            List<Chapter> list = chapterDao.getChapterByFromNovel(fromNovel, offset, count, orderBy);
            return list == null ? List.of() : list;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    // May be never used
    @NonNull
    @Deprecated
    public List<Chapter> findChapterByTitle(@NonNull String title, @NonNull int offset, @NonNull int count, @NonNull String orderBy) {
        try {
            List<Chapter> list = chapterDao.getChapterByTitle(title, offset, count, orderBy);
            return list == null ? List.of() : list;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @NonNull
    public List<Section> findSectionByFromChapter(@NonNull UUID fromChapter, @NonNull int offset, @NonNull int count, @NonNull String orderBy) {
        try {
            List<Section> list = sectionDao.getSectionByFromChapter(fromChapter, offset, count, orderBy);
            return list == null ? List.of() : list;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @NonNull
    public List<Section> searchByContent(@NonNull String keywords, @NonNull int offset, @NonNull int count, @NonNull String orderBy) {
        try {
            List<Section> list = sectionDao.getSectionByKeywords(keywords, offset, count, orderBy);
            return list == null ? List.of() : list;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void destroy() throws Exception {
        future.cancel(true);
        log.info("Service destroying");
        executor.shutdownNow();
    }

    // TODO 热搜与推荐

}
