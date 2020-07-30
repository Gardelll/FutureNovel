package net.wlgzs.futurenovel.model;

import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.apache.ibatis.annotations.AutomapConstructor;

// TODO like/收藏
@Data
@AllArgsConstructor(onConstructor_={@AutomapConstructor})
public class BookSelf implements Serializable {

    private static final long serialVersionUID = 0x459b1be46995d9caL;

    @NonNull
    private final UUID uniqueId;

    @NonNull
    private final UUID accountId;

    @NonNull
    private String title;

    @NonNull
    private ArrayNode novels;

}
