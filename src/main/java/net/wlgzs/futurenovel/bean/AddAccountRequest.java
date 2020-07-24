package net.wlgzs.futurenovel.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import net.wlgzs.futurenovel.model.Account;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AddAccountRequest {

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
     * 电子邮箱地址
     */
    @NotBlank
    @Email
    @Size(max = 255)
    public String email;

    /**
     * 手机号码
     */
    @NotBlank
    @Size(min = 11, max = 14)
    public String phone;

    /**
     * 用户状态
     * @see Account.Status
     */
    public Account.Status status = Account.Status.UNVERIFIED;

    /**
     * 是否为高级会员
     */
    public boolean vip = false;

    /**
     * 权限组
     * @see Account.Permission
     */
    public Account.Permission permission = Account.Permission.USER;

}
