package net.wlgzs.futurenovel.packet;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import net.wlgzs.futurenovel.model.Chapter;
import net.wlgzs.futurenovel.model.Comment;
import net.wlgzs.futurenovel.model.NovelIndex;
import net.wlgzs.futurenovel.model.NovelNode;
import org.apache.ibatis.annotations.AutomapConstructor;

public class Responses {

    @Getter
    @EqualsAndHashCode(callSuper = true)
    public static class CommentInfo extends Comment {

        private final String userName;

        private final String profileImgUrl;

        private final int level;

        private final long total;

        @AutomapConstructor
        public CommentInfo(@NonNull UUID uniqueId,
                           @NonNull UUID accountId,
                           @NonNull UUID sectionId,
                           @NonNull byte rating,
                           @NonNull String text,
                           @NonNull Date createTime,
                           String userName,
                           String profileImgUrl,
                           int level,
                           long total) {
            super(uniqueId, accountId, sectionId, rating, text, createTime);
            this.userName = userName;
            this.profileImgUrl = profileImgUrl;
            this.level = level;
            this.total = total;
        }

    }

    public static class ErrorResponse {
        public String error;
        public String errorMessage;
        public String cause;
        @JsonIgnore
        public int status;
    }

    @EqualsAndHashCode(callSuper = false)
    @Getter
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    @JsonIgnoreProperties({"empty", "size"})
    public static class Novel extends AbstractList<NovelChapter> {

        final private UUID uniqueId;
        final private UUID uploader;
        private final String title;
        private final NovelIndex.Copyright copyright;
        private final List<String> authors;
        private final String description;
        private final byte rating;
        private final List<String> tags;
        private final String series;
        private final String publisher;
        @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy年MM月dd日 HH:mm:ss")
        private final Date pubdate;
        private final long hot;
        private final String coverImgUrl;
        private final LinkedList<NovelChapter> chapters;

        public Novel(@NonNull NovelIndex novelIndex) {
            this.uniqueId = novelIndex.getUniqueId();
            this.uploader = novelIndex.getUploader();
            this.chapters = new LinkedList<>();
            this.title = novelIndex.getTitle();
            this.copyright = novelIndex.getCopyright();
            this.authors = Arrays.asList(novelIndex.getAuthors().split(","));
            this.description = novelIndex.getDescription();
            this.rating = novelIndex.getRating();
            this.tags = Arrays.asList(novelIndex.getTags().split(","));
            this.series = novelIndex.getSeries();
            this.publisher = novelIndex.getPublisher();
            this.pubdate = novelIndex.getPubdate();
            this.hot = novelIndex.getHot();
            this.coverImgUrl = novelIndex.getCoverImgUrl();
        }

        @Override
        public NovelChapter get(int index) {
            return chapters.get(index);
        }

        @Override
        public int size() {
            return chapters.size();
        }

        @Override
        public boolean contains(Object o) {
            return chapters.contains(o);
        }

        @Override
        public ListIterator<NovelChapter> listIterator(int index) {
            return chapters.listIterator(index);
        }

        @Override
        public boolean addAll(int index, Collection<? extends NovelChapter> c) {
            return chapters.addAll(index, c);
        }

        @Override
        public boolean addAll(Collection<? extends NovelChapter> c) {
            return chapters.addAll(c);
        }

        @Override
        public void clear() {
            chapters.clear();
        }

        @Override
        public Spliterator<NovelChapter> spliterator() {
            return chapters.spliterator();
        }

        @Override
        public int indexOf(Object o) {
            return chapters.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            return chapters.lastIndexOf(o);
        }

    }

    @Getter
    @EqualsAndHashCode(callSuper = false)
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    @JsonIgnoreProperties({"size", "empty", "thisUUID", "parentUUID"})
    public static class NovelChapter extends AbstractList<NovelChapter.SectionInfo> implements NovelNode {

        private final UUID uniqueId;
        private final UUID fromNovel;
        private final String title;
        private final LinkedList<SectionInfo> sections;

        public NovelChapter(@NonNull Chapter chapter) {
            this.uniqueId = chapter.getUniqueId();
            this.fromNovel = chapter.getFromNovel();
            this.title = chapter.getTitle();
            this.sections = new LinkedList<>();
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public SectionInfo get(int index) {
            return sections.get(index);
        }

        @Override
        public int size() {
            return sections.size();
        }

        @Override
        public boolean addAll(int index, Collection<? extends SectionInfo> c) {
            return sections.addAll(index, c);
        }

        @Override
        public boolean add(SectionInfo sectionInfo) {
            return sections.add(sectionInfo);
        }

        @Override
        public void add(int index, SectionInfo element) {
            sections.add(index, element);
        }

        @Override
        public boolean addAll(Collection<? extends SectionInfo> c) {
            return sections.addAll(c);
        }

        @Override
        public SectionInfo remove(int index) {
            return sections.remove(index);
        }

        @Override
        public boolean remove(Object o) {
            return sections.remove(o);
        }

        @Override
        public SectionInfo set(int index, SectionInfo element) {
            return sections.set(index, element);
        }

        @Override
        public boolean contains(Object o) {
            return sections.contains(o);
        }

        @Override
        public int indexOf(Object o) {
            return sections.indexOf(o);
        }

        @Override
        public ListIterator<SectionInfo> listIterator(int index) {
            return sections.listIterator(index);
        }

        @Override
        public void clear() {
            sections.clear();
        }

        @Override
        public Spliterator<SectionInfo> spliterator() {
            return sections.spliterator();
        }

        @Override
        public int lastIndexOf(Object o) {
            return sections.lastIndexOf(o);
        }

        @Override
        public UUID getParentUUID() {
            return fromNovel;
        }

        @Override
        public UUID getThisUUID() {
            return uniqueId;
        }

        @Getter
        @AllArgsConstructor(onConstructor_ = {@AutomapConstructor})
        @EqualsAndHashCode
        @JsonIgnoreProperties({"thisUUID", "parentUUID"})
        public static class SectionInfo implements NovelNode {
            final private UUID uniqueId;
            final private UUID fromChapter;
            final private String title;

            public SectionInfo(@NonNull net.wlgzs.futurenovel.model.Section section) {
                this.uniqueId = section.getUniqueId();
                this.fromChapter = section.getFromChapter();
                this.title = section.getTitle();
            }

            @Override
            public UUID getParentUUID() {
                return fromChapter;
            }

            @Override
            public UUID getThisUUID() {
                return uniqueId;
            }

            @Override
            public String getTitle() {
                return title;
            }

        }

    }

}
