package net.wlgzs.futurenovel.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.Size;
import net.wlgzs.futurenovel.model.NovelIndex;
import org.hibernate.validator.constraints.URL;

public class EditNovelRequest {
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
