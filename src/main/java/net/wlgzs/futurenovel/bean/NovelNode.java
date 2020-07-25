package net.wlgzs.futurenovel.bean;

import java.util.UUID;

public interface NovelNode {

    UUID getParentUUID();

    UUID getThisUUID();

    String getTitle();

}
