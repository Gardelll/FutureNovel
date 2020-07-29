package net.wlgzs.futurenovel.model;

import java.util.UUID;
import lombok.Data;

// TODO 阅读历史
@Data
public class ReadHistory {

    private final UUID uniqueId;

    private UUID sectionId;

}
