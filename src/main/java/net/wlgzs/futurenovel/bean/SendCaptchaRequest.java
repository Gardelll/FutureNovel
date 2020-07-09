package net.wlgzs.futurenovel.bean;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class SendCaptchaRequest {
    @NotBlank
    @Email
    public String email;
}
