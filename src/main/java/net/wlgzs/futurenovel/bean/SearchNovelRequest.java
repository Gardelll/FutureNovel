package net.wlgzs.futurenovel.bean;

import java.util.Date;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 搜索请求
 */
@Data
public class SearchNovelRequest {

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
    public SortBy sortBy;

    /**
     * 搜索类型
     */
    @NotNull
    public SearchBy searchBy;

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
        RANDOM("RANDOM()"),
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
