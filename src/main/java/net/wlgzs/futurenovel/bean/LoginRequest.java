package net.wlgzs.futurenovel.bean;

import javax.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank
    public String userName;
    @NotBlank
    public String password;
    public String redirectTo;
}
