package net.wlgzs.futurenovel.model;

import java.util.UUID;
import lombok.Data;

// TODO 评论
@Data
public class Comment {

    private final UUID uniqueId;

    private final UUID accountId;

    private final UUID sectionId;

    private byte rating;

    private String text;

}
