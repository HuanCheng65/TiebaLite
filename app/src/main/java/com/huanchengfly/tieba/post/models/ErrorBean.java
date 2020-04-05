package com.huanchengfly.tieba.post.models;

import com.google.gson.annotations.SerializedName;

public class ErrorBean extends BaseBean {
    @SerializedName("error_code")
    private String errorCode;
    @SerializedName("error_msg")
    private String errorMsg;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}