package net.wlgzs.futurenovel.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.apache.ibatis.annotations.AutomapConstructor;

// TODO 评论
@Data
@AllArgsConstructor(onConstructor_={@AutomapConstructor})
public class Comment implements Serializable {

    private static final long serialVersionUID = 0x840513926246d610L;

    @NonNull
    private final UUID uniqueId;

    @NonNull
    private final UUID accountId;

    @NonNull
    private final UUID sectionId;

    @NonNull
    private byte rating;

    @NonNull
    private String text;

    @NonNull
    private Date createTime;

}
