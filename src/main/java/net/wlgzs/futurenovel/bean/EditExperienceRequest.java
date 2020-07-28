package net.wlgzs.futurenovel.bean;

import java.math.BigInteger;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

public class EditExperienceRequest {
    @NotBlank
    @Size(min = 36, max = 36)
    public String accountId;
    @NotNull
    @Positive
    public BigInteger experience;
}
