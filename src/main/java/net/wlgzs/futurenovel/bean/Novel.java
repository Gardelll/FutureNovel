package net.wlgzs.futurenovel.bean;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import net.wlgzs.futurenovel.model.NovelIndex;

@EqualsAndHashCode(callSuper = false)
@Getter
public class Novel extends AbstractList<NovelChapter> {

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
    private final Date pubdate;
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