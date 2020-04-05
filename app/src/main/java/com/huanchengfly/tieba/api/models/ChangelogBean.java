package com.huanchengfly.tieba.api.models;

import com.google.gson.annotations.SerializedName;
import com.huanchengfly.tieba.post.models.BaseBean;

public class ChangelogBean extends BaseBean {
    @SerializedName("error_code")
    private int errorCode;
    @SerializedName("error_msg")
    private String errorMsg;
    private boolean success;
    private String result;

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getResult() {
        return result;
    }
}
