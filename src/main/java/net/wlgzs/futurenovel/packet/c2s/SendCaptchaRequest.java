package net.wlgzs.futurenovel.packet.c2s;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class SendCaptchaRequest {
    @NotBlank
    @Email
    @Size(max = 255)
    public String email;
}
