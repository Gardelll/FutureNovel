package net.wlgzs.futurenovel.model;

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import net.wlgzs.futurenovel.packet.NovelNode;
import org.apache.ibatis.annotations.AutomapConstructor;

/**
 * 小说的小节
 */
@Data
@AllArgsConstructor(onConstructor_={@AutomapConstructor})
public class Section implements Serializable, NovelNode {

    private static final long serialVersionUID = -1682410254970643692L;

    /**
     * 小节的唯一 ID
     */
    @NonNull
    private UUID uniqueId;

    /**
     * 所属章节的 ID
     */
    @NonNull
    private UUID fromChapter;

    /**
     * 小节标题
     */
    @NonNull
    @Getter(onMethod_ = {@Override})
    private String title;

    /**
     * 文本
     */
    @NonNull
    private String text;

    @Override
    public UUID getParentUUID() {
        return fromChapter;
    }

    @Override
    public UUID getThisUUID() {
        return uniqueId;
    }

}
