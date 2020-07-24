package net.wlgzs.futurenovel.bean;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class LoginRequest {

    /**
     * 用户名或邮箱
     */
    @NotBlank
    @Size(max = 255)
    public String userName;

    /**
     * 密码
     */
    @NotBlank
    @Size(min = 6, max = 255)
    public String password;

    /**
     * 邮箱激活码
     * <p>
     * 当且仅当账号未激活时验证
     */
    @Size(min = 4)
    public String activateCode;

    /**
     * 登录完成后的跳转 Url
     */
    public String redirectTo;

}
