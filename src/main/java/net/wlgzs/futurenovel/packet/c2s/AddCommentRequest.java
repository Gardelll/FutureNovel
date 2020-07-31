package net.wlgzs.futurenovel.packet.c2s;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.Range;

public class AddCommentRequest {
    @NotNull
    @Range(min = 1, max = 10)
    public byte rating;
    @NotBlank
    @Size(min = 1, max = 21845) // 21KB
    public String text;
}
