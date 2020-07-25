package net.wlgzs.futurenovel.model;

import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import net.wlgzs.futurenovel.bean.NovelNode;

/**
 * 小说的章节
 */
@Data
@AllArgsConstructor
public class Chapter implements Serializable, NovelNode {

    private static final long serialVersionUID = 2888176444107291305L;

    /**
     * 章节的唯一 ID
     */
    @NonNull
    private UUID uniqueId;

    /**
     * 所属的小说的 ID
     */
    @NonNull
    private UUID fromNovel;

    /**
     * 章标题
     */
    @NonNull
    @Getter(onMethod_ = {@Override})
    private String title;

    /**
     * 小节索引
     */
    @NonNull
    private ArrayNode sections;

    @Override
    public UUID getParentUUID() {
        return fromNovel;
    }

    @Override
    public UUID getThisUUID() {
        return uniqueId;
    }

}
