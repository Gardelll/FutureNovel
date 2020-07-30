package net.wlgzs.futurenovel.model;

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.apache.ibatis.annotations.AutomapConstructor;

// TODO 阅读历史
@Data
@AllArgsConstructor(onConstructor_={@AutomapConstructor})
public class ReadHistory implements Serializable {

    private static final long serialVersionUID = 0x3e91d66f0c268a28L;

    @NonNull
    private final UUID uniqueId;

    @NonNull
    private final UUID accountId;

    @NonNull
    private UUID sectionId;

}
