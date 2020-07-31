package net.wlgzs.futurenovel.packet.c2s;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

public class GetReadHistoryRequest {
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy年MM月dd日 HH:mm:ss")
    public Date after;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy年MM月dd日 HH:mm:ss")
    public Date before;
}
