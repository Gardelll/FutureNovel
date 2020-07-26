package net.wlgzs.futurenovel.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import net.wlgzs.futurenovel.bean.NovelChapter;
import net.wlgzs.futurenovel.dao.ChapterDao;
import net.wlgzs.futurenovel.dao.NovelIndexDao;
import net.wlgzs.futurenovel.dao.SectionDao;
import net.wlgzs.futurenovel.exception.FutureNovelException;
import net.wlgzs.futurenovel.model.Account;
import net.wlgzs.futurenovel.model.Chapter;
import net.wlgzs.futurenovel.model.NovelIndex;
import net.wlgzs.futurenovel.model.Section;
import net.wlgzs.futurenovel.utils.NovelNodeComparator;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * 小说的增删改查
 */
@Service
@Slf4j
public class NovelService implements DisposableBean {

    private final NovelIndexDao novelIndexDao;

    private final ChapterDao chapterDao;

    private final SectionDao sectionDao;

    @Getter(onMethod_ = {@Synchronized})
    private final HashSet<String> tags = new HashSet<>();

    @Getter(onMethod_ = {@Synchronized})
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
                tags.clear();
                series.clear();
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
                } while (result.size() >= count && series.size() < 1000);
            }
        }, 1, 10, TimeUnit.MINUTES);
    }

    /**
     * 创建小说目录
     * @param account 所用帐号
     * @param copyright 版权
     * @param title 标题
     * @param authors 作者
     * @param description 描述
     * @param rating 评分
     * @param tags 标签
     * @param series 系列
     * @param publisher 出版社
     * @param pubdate 出版日期
     * @param coverImgUrl 封面图
     * @return NovelIndex 小说目录
     * @see NovelIndex
     * @see net.wlgzs.futurenovel.model.NovelIndex.Copyright
     * @see net.wlgzs.futurenovel.model.Account.Permission
     */
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
                    title,
                    copyright,
                    String.join(",", authors),
                    description,
                    rating,
                    String.join(",", tags),
                    series,
                    publisher,
                    pubdate,
                    0,
                    coverImgUrl,
                    new ArrayNode(JsonNodeFactory.instance));
            var ret = novelIndexDao.insertNovelIndex(novelIndex);
            if (ret != 1) throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "逐步添加操作返回了不是 1 的值：" + ret);
            return novelIndex;
        } catch (DataAccessException e) {
            log.warn("database error: ", e);
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @NonNull
    public NovelIndex getNovelIndex(@NonNull UUID uniqueId) {
        try {
            NovelIndex ret = novelIndexDao.getNovelIndexById(uniqueId);
            if (ret != null) return ret;
        } catch (DataAccessException e) {
            log.warn("database error: ", e);
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @NonNull
    @Transactional
    public Chapter addChapter(@NonNull Account account,
                              @NonNull NovelIndex novelIndex,
                              @Nullable String title) {
        if (novelIndex.getUploader().equals(account.getUid())) account.checkPermission();
        else account.checkPermission(Account.Permission.ADMIN);
        try {
            if (title == null || title.isBlank()) title = String.format("第 %d 章", chapterDao.countChapterByFromNovel(novelIndex.getUniqueId()) + 1);
            var chapter = new Chapter(UUID.randomUUID(), novelIndex.getUniqueId(), title.trim(), new ArrayNode(JsonNodeFactory.instance));
            var arrayNode = novelIndex.getChapters();
            arrayNode.add(chapter.getUniqueId().toString());
            var ret = chapterDao.insertChapter(chapter);
            if (ret != 1) throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "逐步添加操作返回了不是 1 的值：" + ret);
            ret = novelIndexDao.updateNovelIndex(novelIndex);
            if (ret != 1) throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "更新添加操作返回了不是 1 的值：" + ret);
            return chapter;
        } catch (DataAccessException e) {
            log.warn("database error: ", e);
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @NonNull
    public Chapter getChapter(@NonNull UUID uniqueId) {
        try {
            var ret = chapterDao.getChapterById(uniqueId);
            if (ret != null) return ret;
        } catch (DataAccessException e) {
            log.warn("database error: ", e);
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @NonNull
    @Transactional
    public Section addSection(@NonNull Account account,
                              @NonNull Chapter chapter,
                              @Nullable NovelIndex novelIndex,
                              @Nullable String title,
                              @NonNull String text) {
        try {
            if (novelIndex == null) novelIndex = novelIndexDao.getNovelIndexById(chapter.getFromNovel());
            if (novelIndex == null) throw new FutureNovelException(FutureNovelException.Error.NOVEL_NOT_FOUND);
            if (account.getUid().equals(novelIndex.getUploader())) account.checkPermission();
            else account.checkPermission(Account.Permission.ADMIN);
            if (title == null || title.isBlank()) title = String.format("第 %d 节", sectionDao.countSectionByFromChapter(chapter.getUniqueId()) + 1);
            var section = new Section(UUID.randomUUID(), chapter.getUniqueId(), title.trim(), text);
            var arrayNode = chapter.getSections();
            arrayNode.add(section.getUniqueId().toString());
            var ret = sectionDao.insertSection(section);
            if (ret != 1) throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "逐步添加操作返回了不是 1 的值：" + ret);
            ret = chapterDao.updateChapter(chapter);
            if (ret != 1) throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "更新操作返回了不是 1 的值：" + ret);
            return section;
        } catch (DataAccessException e) {
            log.warn("database error: ", e);
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @NonNull
    public Section getSection(@NonNull UUID uniqueId) {
        try {
            var ret = sectionDao.getSectionById(uniqueId);
            if (ret != null) return ret;
        } catch (DataAccessException e) {
            log.warn("database error: ", e);
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @NonNull
    public List<NovelChapter.SectionInfo> getSectionInfoByFromChapter(@NonNull UUID fromChapter) {
        try {
            var list = sectionDao.getSectionInfoByFromChapter(fromChapter);
            return list == null ? List.of() : list;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @NonNull
    public List<NovelChapter.SectionInfo> getSectionInfoByFromChapterList(@NonNull List<UUID> fromChapterList) {
        if (fromChapterList.isEmpty()) return List.of();
        try {
            var list = sectionDao.getSectionInfoByFromChapterList(fromChapterList);
            return list == null ? List.of() : list;
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
            var chapterIdList = new LinkedList<UUID>();
            for (JsonNode node : arrayNode) chapterIdList.add(UUID.fromString(node.asText()));
            try {
                sectionDao.deleteSectionByFromChapterList(chapterIdList);
            } catch (Exception e) {
                log.warn("chapter(s) from novel({}) can not totally delete", novelIndex.getUniqueId().toString());
            }
            try {
                chapterDao.deleteChapterByFromNovel(novelIndex.getUniqueId());
            } catch (Exception e) {
                log.warn("chapter(s) from novel({}) can not totally delete", novelIndex.getUniqueId().toString());
            }
            var ret = novelIndexDao.deleteNovelIndex(novelIndex);
            if (ret != 1) throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "逐步删除操作返回了不是 1 的值：" + ret);
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public void deleteChapter(@NonNull Account account, @NonNull UUID chapterId) {
        try {
            var novelIndex = novelIndexDao.getNovelIndexByChapterId(chapterId);
            if (account.getUid().equals(novelIndex.getUploader())) account.checkPermission();
            else account.checkPermission(Account.Permission.ADMIN);
            try {
                sectionDao.deleteSectionByFromChapter(chapterId);
            } catch (Exception e) {
                log.warn("section(s) from novel({}) can not delete", novelIndex.getUniqueId().toString());
            }
            var ret = chapterDao.deleteChapterById(chapterId);
            if (ret != 1) throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "逐步删除操作返回了不是 1 的值：" + ret);
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @Transactional
    public void deleteSection(@NonNull Account account, @NonNull UUID sectionId) {
        try {
            var novelIndex = novelIndexDao.getNovelIndexBySectionId(sectionId);
            if (account.getUid().equals(novelIndex.getUploader())) account.checkPermission();
            else account.checkPermission(Account.Permission.ADMIN);
            var ret = sectionDao.deleteSectionById(sectionId);
            if (ret != 1) throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, "逐步删除操作返回了不是 1 的值：" + ret);
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    public boolean hotAddOne(@NonNull UUID novelIndexId) {
        try {
            return 1 == novelIndexDao.hotAddOne(novelIndexId);
        } catch (DataAccessException e) {
            log.warn("error while increment hot", e);
            return false;
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
    public List<NovelIndex> findNovelIndexByUploader(@NonNull UUID uploader, @NonNull int offset, @NonNull int count, @Nullable String orderBy) {
        try {
            List<NovelIndex> list = novelIndexDao.getNovelIndexByUploader(uploader, offset, count, orderBy);
            return list == null ? List.of() : list;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @NonNull
    public List<NovelIndex> findNovelIndexByPubDate(@NonNull Date after, @NonNull Date before, @NonNull int offset, @NonNull int count, @Nullable String orderBy) {
        try {
            List<NovelIndex> list = novelIndexDao.getNovelIndexByPubDate(after, before, offset, count, orderBy);
            return list == null ? List.of() : list;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @NonNull
    public List<NovelIndex> searchNovelIndex(@NonNull String keywords, @NonNull int offset, @NonNull int count, @Nullable String orderBy) {
        try {
            List<NovelIndex> list = novelIndexDao.getNovelIndexByKeywords(keywords, offset, count, orderBy);
            return list == null ? List.of() : list;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @NonNull
    public List<Chapter> findChapterByFromNovel(@NonNull UUID fromNovel, @NonNull int offset, @NonNull int count, @Nullable String orderBy) {
        try {
            List<Chapter> list = chapterDao.getChapterByFromNovel(fromNovel, offset, count, orderBy);
            if (list == null) return List.of();
            list.sort(NovelNodeComparator::compareByTitle);
            return list;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    // May be never used
    @NonNull
    @Deprecated
    public List<Chapter> findChapterByTitle(@NonNull String title, @NonNull int offset, @NonNull int count, @Nullable String orderBy) {
        try {
            List<Chapter> list = chapterDao.getChapterByTitle(title, offset, count, orderBy);
            return list == null ? List.of() : list;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @NonNull
    public List<Section> findSectionByFromChapter(@NonNull UUID fromChapter, @NonNull int offset, @NonNull int count, @Nullable String orderBy) {
        try {
            List<Section> list = sectionDao.getSectionByFromChapter(fromChapter, offset, count, orderBy);
            return list == null ? List.of() : list;
        } catch (DataAccessException e) {
            throw new FutureNovelException(FutureNovelException.Error.DATABASE_EXCEPTION, e.getLocalizedMessage(), e);
        }
    }

    @NonNull
    public List<Section> searchByContent(@NonNull String keywords, @NonNull int offset, @NonNull int count, @Nullable String orderBy) {
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
