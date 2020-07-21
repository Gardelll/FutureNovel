package net.wlgzs.futurenovel.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

/**
 * 编辑用户信息
 * 请求参数
 * 不包含的属性不会修改
 * 未发生任何改动则认为修改失败
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EditAccountRequest {

    @JsonIgnore
    public UUID uid;

    /**
     * 用户 UID
     */
    @Size(min = 36)
    public String uuid;

    /**
     * 用户名
     */
    @Size(min = 1)
    public String userName;

    /**
     * 密码
     */
    @Size(min = 6)
    public String password;

    /**
     * 邮箱地址
     */
    @Email
    public String email;

    /**
     * 手机号码
     */
    @Size(min = 11, max = 14)
    public String phone;

    /**
     * 头像图像 URL 地址
     */
    @URL
    public String profileImgUrl;

    /**
     * 邮箱激活码
     * 当且仅当修改邮箱、密码时验证
     */
    @Size(min = 4)
    public String activateCode;

}
