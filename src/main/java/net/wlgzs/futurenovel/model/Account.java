package net.wlgzs.futurenovel.model;

import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Data
public class Account implements Serializable {
    private static final long serialVersionUID = -806044765486901910L;

    final private UUID uid;
    @NonNull
    @org.springframework.lang.NonNull
    private String userName;
    @NonNull
    @org.springframework.lang.NonNull
    private String userPass; // 用户密码
    @NonNull
    @org.springframework.lang.NonNull
    private String email;
    private String phone;
    @NonNull
    @org.springframework.lang.NonNull
    private String registerIP; // 注册 IP
    private String lastLoginIP; // 上次登录操作的 IP
    @NonNull
    @org.springframework.lang.NonNull
    private Date registerDate; // 注册日期与时间
    private Date lastLoginDate; // 上次登录操作的日期与时间
    @NonNull
    @org.springframework.lang.NonNull
    private Status status; // 用户的状态
    @NonNull
    @org.springframework.lang.NonNull
    private Permission permission; // 用户的权限组
    @NonNull
    @org.springframework.lang.NonNull
    private long experience; // 经验/积分
    private String profileImgUrl; // 头像 Url

    public Account() {
        uid = UUID.randomUUID();
    }

    public String getUidNum() {
        return uid.toString().replaceAll("-", "");
    }

    /**
     * 根据经验计算等级
            递推项：
            1-100    Lv0
            101-300  Lv1
            301-600  Lv2
            601-1000 Lv3
            ......
            故起始经验的通项公式为：
            exp=0.5*(Lv*Lv+Lv)*100
            所以解得等级的计算公式为：
            Lv=0.5*(sqrt(8*exp/100+1)-1)
     * @return 用户等级
     */
    public int getLevel() {
        return (int) (0.5 * (Math.sqrt(0.08 * experience + 1) - 1));
    }

    public enum Status {
        FINED, // 账号正常
        UNVERIFIED, // 账号未验证邮箱
        BANED // 账号被封禁
    }

    public enum Permission {
        USER,
        AUTHOR,
        VIP,
        ADMIN
    }
}
