package net.wlgzs.futurenovel.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.apache.ibatis.annotations.AutomapConstructor;

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
    private final byte rating;

    @NonNull
    private final String text;

    @NonNull
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy年MM月dd日 HH:mm:ss")
    private final Date createTime;

}
