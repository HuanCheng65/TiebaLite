package com.huanchengfly.tieba.post.api.models;

import com.google.gson.annotations.SerializedName;

public class WebReplyResultBean {
    @SerializedName("no")
    private int errorCode;
    @SerializedName("error")
    private String errorMsg;
    private WebReplyDataBean data;

    public WebReplyDataBean getData() {
        return data;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public static class WebReplyDataBean {
        @SerializedName("is_not_top_stick")
        private int isNotTopStick;
        private long pid;
        private long tid;

        public int getIsNotTopStick() {
            return isNotTopStick;
        }

        public long getPid() {
            return pid;
        }

        public long getTid() {
            return tid;
        }
    }
}
