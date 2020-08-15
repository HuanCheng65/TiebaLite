package com.huanchengfly.tieba.post.api.models.web;

import com.google.gson.annotations.SerializedName;
import com.huanchengfly.tieba.post.models.BaseBean;

public class WebBaseBean<Data> extends BaseBean {
    @SerializedName("no")
    private int errorCode;
    @SerializedName("error")
    private String errorMsg;
    private Data data;

    public int getErrorCode() {
        return errorCode;
    }

    public WebBaseBean<Data> setErrorCode(int errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public WebBaseBean<Data> setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
        return this;
    }

    public Data getData() {
        return data;
    }

    public WebBaseBean<Data> setData(Data data) {
        this.data = data;
        return this;
    }
}
