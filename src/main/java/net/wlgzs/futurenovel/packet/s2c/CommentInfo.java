package net.wlgzs.futurenovel.packet.s2c;

import java.util.Date;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import net.wlgzs.futurenovel.model.Comment;
import org.apache.ibatis.annotations.AutomapConstructor;

@Getter
@EqualsAndHashCode(callSuper = true)
public class CommentInfo extends Comment {

    private final String userName;

    private final String profileImgUrl;

    private final int level;

    private final long total;

    @AutomapConstructor
    public CommentInfo(@NonNull UUID uniqueId,
                       @NonNull UUID accountId,
                       @NonNull UUID sectionId,
                       @NonNull byte rating,
                       @NonNull String text,
                       @NonNull Date createTime,
                       String userName,
                       String profileImgUrl,
                       int level,
                       long total) {
        super(uniqueId, accountId, sectionId, rating, text, createTime);
        this.userName = userName;
        this.profileImgUrl = profileImgUrl;
        this.level = level;
        this.total = total;
    }

}
