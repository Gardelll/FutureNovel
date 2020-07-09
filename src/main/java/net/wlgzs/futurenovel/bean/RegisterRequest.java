package net.wlgzs.futurenovel.bean;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank
    public String userName;
    @NotBlank
    public String password;
    @NotBlank
    @Email
    public String email;
    @NotBlank
    @Size(min = 4)
    public String activateCode;
    @NotBlank
    @Size(min = 11, max = 14)
    public String phone;
    public String redirectTo;
}
