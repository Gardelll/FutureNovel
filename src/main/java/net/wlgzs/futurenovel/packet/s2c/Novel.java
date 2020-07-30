package net.wlgzs.futurenovel.packet.s2c;

import com.fasterxml.jackson.annotation.JsonFormat;
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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import net.wlgzs.futurenovel.model.NovelIndex;
import net.wlgzs.futurenovel.utils.NovelNodeComparator;

@EqualsAndHashCode(callSuper = false)
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonIgnoreProperties({"empty", "size"})
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
        boolean b = chapters.addAll(index, c);
        chapters.sort(NovelNodeComparator::compareByTitle);
        return b;
    }

    @Override
    public boolean addAll(Collection<? extends NovelChapter> c) {
        boolean b = chapters.addAll(c);
        chapters.sort(NovelNodeComparator::compareByTitle);
        return b;
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
