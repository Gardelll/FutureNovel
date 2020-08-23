/*
 *  Copyright (C) 2020 Future Studio
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.wlgzs.futurenovel.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import net.wlgzs.futurenovel.exception.FutureNovelException;
import org.apache.ibatis.annotations.AutomapConstructor;

@Data
@AllArgsConstructor(onConstructor_={@AutomapConstructor})
public class Account implements Serializable {

    private static final long serialVersionUID = -1048142809503199485L;

    /**
     * 用户的 ID
     */
    @NonNull
    final private UUID uid;

    /**
     * 用户名
     */
    @NonNull
    private String userName;

    /**
     * 密码（已加密）
     */
    @NonNull
    @JsonIgnore
    private String userPass;

    /**
     * 邮箱地址
     */
    @NonNull
    private String email;

    /**
     * 手机号码，不会包含区号 (+86...)
     */
    private String phone;

    /**
     * 注册 IP
     */
    @NonNull
    private String registerIP;

    /**
     * 上次登陆 IP
     */
    private String lastLoginIP;

    /**
     * 注册时间
     */
    @NonNull
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy年MM月dd日 HH:mm:ss")
    private Date registerDate;

    /**
     * 上次登陆时间
     */
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy年MM月dd日 HH:mm:ss")
    private Date lastLoginDate;

    /**
     * 当前用户状态
     * @see Status
     */
    @NonNull
    private Status status;

    /**
     * 是否为高级会员
     */
    @NonNull
    private boolean isVIP;

    /**
     * 权限组
     * @see Permission
     */
    @NonNull
    private Permission permission;

    /**
     * 经验/积分
     */
    @NonNull
    private BigInteger experience;

    /**
     * 用户头像的 URL
     */
    private String profileImgUrl;

    public Account() {
        uid = UUID.randomUUID();
    }

    /**
     * 获取纯数字格式的 UID
     * @return 用户 ID（不含连字符）
     */
    public String getUidNum() {
        return uid.toString().replaceAll("-", "");
    }

    /**
     * 根据经验计算等级<br />
     *      递推项：<br />
     *      1-100    Lv0 <br />
     *      101-300  Lv1 <br />
     *      301-600  Lv2 <br />
     *      601-1000 Lv3 <br />
     *      ...... <br />
     *      故起始经验的通项公式为： <br />
     *      exp=0.5*(Lv*Lv+Lv)*100 <br />
     *      所以解得等级的计算公式为： <br />
     *      Lv=0.5*(sqrt(8*exp/100+1)-1) <br />
     * @return 用户等级
     */
    public int getLevel() {
        return BigDecimal.valueOf(0.5)
            .multiply(BigDecimal.valueOf(0.08)
                .multiply(new BigDecimal(experience))
                .add(BigDecimal.ONE)
                .sqrt(new MathContext(7, RoundingMode.HALF_UP))
                .subtract(BigDecimal.ONE))
            .intValue();
        //return (int) (0.5 * (Math.sqrt(0.08 * experience + 1) - 1));
    }

    public void checkPermission(Account.Permission ... permissions) throws FutureNovelException {
        if (getStatus() != Account.Status.FINE) throw new FutureNovelException(FutureNovelException.Error.ACCESS_DENIED);
        if (permissions.length == 0) return;
        for (var permission : permissions) {
            if (permission == getPermission()) return;
        }
        throw new FutureNovelException(FutureNovelException.Error.PERMISSION_DENIED);
    }

    public enum Status {
        /**
         * 正常
         */
        FINE,
        /**
         * 未验证
         */
        UNVERIFIED,
        /**
         * 封禁
         */
        BANED
    }

    public enum Permission {
        /**
         * 普通用户<br />
         * 可浏览，发布转载、公版小说
         */
        USER,
        /**
         * 认证作家<br />
         * 可以发布同人、原创小说
         */
        AUTHOR,
        /**
         * 管理员<br />
         * 有所有权限
         */
        ADMIN
    }
}
