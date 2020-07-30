package net.wlgzs.futurenovel.packet.c2s;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.springframework.lang.Nullable;

public class AddSectionRequest {
    @Nullable
    @Size(max = 1024)
    public String title;
    @NotBlank
    @Size(min = 200, max = 4194304) // 最大 4MB
    public String text;
}
