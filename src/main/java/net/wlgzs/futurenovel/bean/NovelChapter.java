package net.wlgzs.futurenovel.bean;

import java.util.AbstractList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import net.wlgzs.futurenovel.model.Chapter;
import net.wlgzs.futurenovel.utils.NovelNodeComparator;
import org.apache.ibatis.annotations.AutomapConstructor;

@Getter
@EqualsAndHashCode(callSuper = false)
public class NovelChapter extends AbstractList<NovelChapter.SectionInfo> implements NovelNode {

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
        boolean b = sections.addAll(index, c);
        sections.sort(NovelNodeComparator::compareByTitle);
        return b;
    }

    @Override
    public boolean addAll(Collection<? extends SectionInfo> c) {
        boolean b = sections.addAll(c);
        sections.sort(NovelNodeComparator::compareByTitle);
        return b;
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
