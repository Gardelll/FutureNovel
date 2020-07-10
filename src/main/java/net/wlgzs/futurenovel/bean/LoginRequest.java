package net.wlgzs.futurenovel.bean;

import javax.validation.constraints.NotBlank;

public class LoginRequest {

    /**
     * 用户名或邮箱
     */
    @NotBlank
    public String userName;

    /**
     * 密码
     */
    @NotBlank
    public String password;

    /**
     * 登录完成后的跳转 Url
     */
    public String redirectTo;
}
