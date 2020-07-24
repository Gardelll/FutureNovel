package net.wlgzs.futurenovel.bean;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求
 */
@Data
public class RegisterRequest {

    /**
     * 用户名
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
     * 再输一遍密码
     */
    @NotBlank
    @Size(min = 6, max = 255)
    public String passwordRepeat;

    /**
     * 电子邮箱地址
     */
    @NotBlank
    @Email
    @Size(max = 255)
    public String email;

    /**
     * 邮箱激活码
     */
    @NotBlank
    @Size(min = 4)
    public String activateCode;

    /**
     * 手机号码
     */
    @NotBlank
    @Size(min = 11, max = 14)
    public String phone;

    /**
     * 跳转地址
     */
    public String redirectTo;
}
