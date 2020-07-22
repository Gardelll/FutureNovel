package net.wlgzs.futurenovel.bean;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.springframework.lang.Nullable;

public class AddSectionRequest {
    @Nullable
    public String title;
    @NotBlank
    @Size(min = 200)
    public String text;
}
