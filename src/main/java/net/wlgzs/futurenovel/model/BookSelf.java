package net.wlgzs.futurenovel.model;

import java.util.UUID;
import lombok.Data;

// TODO like/收藏
@Data
public class BookSelf {

    private final UUID uniqueId;

    private UUID novelIndexId;

}
