package net.wlgzs.futurenovel.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.AutomapConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor(onConstructor_={@AutomapConstructor})
public class ReadHistory implements Serializable {

    private static final long serialVersionUID = 0xa3252f0c7328ddbeL;

    @NonNull
    private final UUID uniqueId;

    @NonNull
    private final UUID accountId;

    @NonNull
    private final UUID sectionId;

    @NonNull
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy年MM月dd日 HH:mm:ss")
    private final Date createTime;

    private String title;

    private UUID novelIndexId;

    private String novelTitle;

    private String chapterTitle;

    private String coverImgUrl;

    private String authors;

}
