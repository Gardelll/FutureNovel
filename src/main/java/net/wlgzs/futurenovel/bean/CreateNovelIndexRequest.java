package net.wlgzs.futurenovel.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import net.wlgzs.futurenovel.model.NovelIndex;
import org.springframework.lang.Nullable;

/**
 * 创建小说目录请求
 * @see NovelIndex
 */
public class CreateNovelIndexRequest {

    /**
     * 版权
     * @see net.wlgzs.futurenovel.model.NovelIndex.Copyright
     * @see net.wlgzs.futurenovel.model.Account.Permission
     */
    @NotNull
    public NovelIndex.Copyright copyright;

    /**
     * 标题
     */
    @NotBlank
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
    public String series;

    /**
     * 出版社
     */
    @NotEmpty
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
    public String coverImgUrl;

}
