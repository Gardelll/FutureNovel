package net.wlgzs.futurenovel.packet;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import lombok.Data;
import net.wlgzs.futurenovel.model.Account;
import net.wlgzs.futurenovel.model.NovelIndex;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;
import org.springframework.lang.Nullable;

public class Requests {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddAccountRequest {

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
         *
         * @see Account.Status
         */
        public Account.Status status = Account.Status.UNVERIFIED;

        /**
         * 是否为高级会员
         */
        public boolean vip = false;

        /**
         * 权限组
         *
         * @see Account.Permission
         */
        public Account.Permission permission = Account.Permission.USER;

    }

    public static class AddChapterRequest {
        @Nullable
        @Size(max = 1024)
        public String title;
        @Nullable
        @Size(min = 1)
        @JsonProperty("sections")
        public List<String> sectionsEdit;
        @Nullable
        @JsonIgnore
        public ArrayNode sections;
    }

    public static class AddCommentRequest {
        @NotNull
        @Range(min = 1, max = 10)
        public byte rating;
        @NotBlank
        @Size(min = 1, max = 21845) // 21KB
        public String text;
    }

    public static class AddSectionRequest {
        @Nullable
        @Size(max = 1024)
        public String title;
        @NotBlank
        @Size(min = 200, max = 4194304) // 最大 4MB
        public String text;
    }

    public static class BookSelfAddNovelRequest {
        @NotBlank
        @Size(min = 36, max = 36)
        public String novelIndexId;
    }

    public static class CreateBookSelfRequest {
        @Size(min = 1)
        public String title;
    }

    /**
     * 创建小说目录请求
     *
     * @see NovelIndex
     */
    public static class CreateNovelIndexRequest {

        /**
         * 版权
         *
         * @see NovelIndex.Copyright
         * @see Account.Permission
         */
        @NotNull
        public NovelIndex.Copyright copyright;

        /**
         * 标题
         */
        @NotBlank
        @Size(max = 1024)
        public String title;

        /**
         * 作者
         */
        @NotEmpty
        public List<String> authors;

        /**
         * 简介
         */
        @NotBlank
        @Size(max = 4194304)
        public String description;

        /**
         * 标签
         */
        @NotEmpty
        public List<String> tags;

        /**
         * 系列
         */
        @Nullable
        @Size(max = 1024)
        public String series;

        /**
         * 出版社
         */
        @NotEmpty
        @Size(max = 1024)
        public String publisher;

        /**
         * 出版日期
         */
        @NotNull
        @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy年MM月dd日 HH:mm:ss")
        public Date pubdate;

        /**
         * 封面图像 URL
         */
        @Nullable
        @URL
        @Size(max = 4194304)
        public String coverImgUrl;

    }

    /**
     * 编辑用户信息
     * <p>
     * 请求参数
     * <p>
     * 不包含的属性不会修改
     * <p>
     * 未发生任何改动则认为修改失败
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EditAccountRequest {

        @JsonIgnore
        public UUID uid;

        /**
         * 用户 UID
         */
        @Size(min = 36, max = 36)
        public String uuid;

        /**
         * 用户名
         */
        @Size(min = 1, max = 255)
        public String userName;

        /**
         * 密码
         */
        @Size(min = 6, max = 255)
        public String password;

        /**
         * 邮箱地址
         */
        @Email
        @Size(max = 255)
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
        @Size(max = 4194304)
        public String profileImgUrl;

        /**
         * 当前用户状态
         *
         * @see Account.Status
         */
        public Account.Status status;

        /**
         * 是否为高级会员
         */
        public Boolean vip;

        /**
         * 权限组
         *
         * @see Account.Permission
         */
        public Account.Permission permission;

        /**
         * 邮箱激活码
         * <p>
         * 当且仅当修改邮箱、密码时验证
         */
        @Size(min = 4)
        public String activateCode;

    }

    public static class EditExperienceRequest {
        @NotBlank
        @Size(min = 36, max = 36)
        public String accountId;
        @NotNull
        @Positive
        public BigInteger experience;
    }

    public static class EditNovelRequest {
        @Size(min = 1)
        public String title;
        public NovelIndex.Copyright copyright;
        @Size(min = 1)
        public List<String> authors;
        @Size(min = 1)
        public String description;
        @Size(min = 1)
        public List<String> tags;
        @Size(min = 1)
        public String series;
        @Size(min = 1)
        public String publisher;
        @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy年MM月dd日 HH:mm:ss")
        public Date pubdate;
        @URL
        public String coverImgUrl;
        @Size(min = 1)
        public List<String> chapters;
    }

    public static class GetReadHistoryRequest {
        @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy年MM月dd日 HH:mm:ss")
        public Date after;
        @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy年MM月dd日 HH:mm:ss")
        public Date before;
    }

    public static class LoginRequest {

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

    /**
     * 注册请求
     */
    @Data
    public static class RegisterRequest {

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

    /**
     * 搜索请求
     */
    @Data
    public static class SearchNovelRequest {

        /**
         * 查询包含关键词，空格分隔
         */
        @Size(min = 2, max = 4096)
        public String keywords;

        /**
         * 查询排除关键词，空格分隔
         */
        @Size(min = 1, max = 4096)
        public String except;

        /**
         * 发布日期从...开始
         */
        public Date after;

        /**
         * 发布日期到...结束
         */
        public Date before;

        /**
         * 排序方式
         */
        @NotNull
        public Requests.SearchNovelRequest.SortBy sortBy;

        /**
         * 搜索类型
         */
        @NotNull
        public Requests.SearchNovelRequest.SearchBy searchBy;

        public enum SearchBy {
            KEYWORDS,
            CONTENT,
            PUBDATE,
            HOT
        }

        public enum SortBy {

            AUTHORS("`authors`"),
            AUTHORS_DESC("`authors` DESC"),
            COPYRIGHT("`copyright`"),
            COPYRIGHT_DESC("`copyright` DESC"),
            HOT("`hot`"),
            HOT_DESC("`hot` DESC"),
            PUBDATE("`pubdate`"),
            PUBDATE_DESC("`pubdate` DESC"),
            PUBLISHER("`publisher`"),
            PUBLISHER_DESC("`publisher` DESC"),
            RATING("`rating`"),
            RATING_DESC("`rating` DESC"),
            SERIES("`series`"),
            SERIES_DESC("`series` DESC"),
            TAGS("`tags`"),
            TAGS_DESC("`tags` DESC"),
            TITLE("`title`"),
            TITLE_DESC("`title` DESC"),
            UPLOADER("`uploader`"),
            UPLOADER_DESC("`uploader` DESC"),
            CREATE_TIME("`createTime`"),
            CREATE_TIME_DESC("`createTime` DESC"),
            RANDOM("RAND()"),
            BEST_MATCH(null);

            private final String orderBy;

            SortBy(String orderBy) {
                this.orderBy = orderBy;
            }

            public String getOrderBy() {
                return orderBy;
            }

        }

    }

    public static class SendCaptchaRequest {
        @NotBlank
        @Email
        @Size(max = 255)
        public String email;
    }

}
