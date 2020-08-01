package net.wlgzs.futurenovel.packet.c2s;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class BookSelfAddNovelRequest {
    @NotBlank
    @Size(min = 36, max = 36)
    public String novelIndexId;
}
