package net.wlgzs.futurenovel.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.List;
import javax.validation.constraints.Size;
import org.springframework.lang.Nullable;

public class AddChapterRequest {
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
