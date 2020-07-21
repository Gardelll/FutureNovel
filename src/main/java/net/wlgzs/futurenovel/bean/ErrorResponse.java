package net.wlgzs.futurenovel.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ErrorResponse {
    public String error;
    public String errorMessage;
    public String cause;
    @JsonIgnore
    public int status;
}
