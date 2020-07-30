package net.wlgzs.futurenovel.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.apache.ibatis.annotations.AutomapConstructor;

/**
 * 小说目录
 */
@Data
@AllArgsConstructor(onConstructor_={@AutomapConstructor})
public class NovelIndex implements Serializable {

    private static final long serialVersionUID = 4627302502994711642L;

    /**
     * 小说的唯一 ID
     */
    @NonNull
    final private UUID uniqueId;

    /**
     * 上传者的 ID
     */
    @NonNull
    final private UUID uploader;

    /**
     * 小说的标题
     */
    @NonNull
    private String title;

    /**
     * 版权
     * @see Copyright
     */
    @NonNull
    private Copyright copyright;

    /**
     * 小说的作者
     */
    @NonNull
    private String authors;

    /**
     * 介绍
     */
    @NonNull
    private String description;

    /**
     * 评分
     * 0-10 分
     * 0 为暂无评价
     */
    @NonNull
    private byte rating;

    /**
     * 标签
     */
    @NonNull
    private String tags;

    /**
     * 系列
     */
    @NonNull
    private String series;

    /**
     * 出版社
     */
    @NonNull
    private String publisher;

    /**
     * 出版日期
     */
    @NonNull
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy年MM月dd日 HH:mm:ss")
    private Date pubdate;

    /**
     * 热度（点击量）
     */
    @NonNull
    private long hot;

    /**
     * 封面图的地址
     */
    private String coverImgUrl;

    /**
     * 章节内容的索引
     */
    @NonNull
    private ArrayNode chapters;

    public enum Copyright {
        /**
         * 公版
         */
        NO_COPYRIGHT,
        /**
         * 转载
         */
        REPRINT,
        /**
         * 同人（二创）
         */
        FAN_FICTION,
        /**
         * 原创
         */
        ORIGINAL
    }

}
