package net.wlgzs.futurenovel.model;

import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.apache.ibatis.annotations.AutomapConstructor;

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

    public void addNovel(@NonNull UUID novelIndexId) {
        novels.add(novelIndexId.toString());
    }

    public void clear() {
        novels.removeAll();
    }

    public void remove(@NonNull UUID novelIndexId) {
        for (int i = 0; i < novels.size(); i++) {
            if (novels.get(i).asText().equals(novelIndexId.toString()))
                novels.remove(i);
        }
    }

}
