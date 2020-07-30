package net.wlgzs.futurenovel.packet;

import java.util.UUID;

public interface NovelNode {

    /**
     * @return 父节点 ID
     */
    UUID getParentUUID();

    /**
     * @return 此节点的 ID
     */
    UUID getThisUUID();

    /**
     * @return 标题
     */
    String getTitle();

}
