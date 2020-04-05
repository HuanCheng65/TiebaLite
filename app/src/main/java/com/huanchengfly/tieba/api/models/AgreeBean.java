package com.huanchengfly.tieba.api.models;

import com.google.gson.annotations.SerializedName;
import com.huanchengfly.tieba.post.models.BaseBean;

public class AgreeBean extends BaseBean {
    @SerializedName("error_code")
    private String errorCode;
    @SerializedName("error_msg")
    private String errorMsg;
    private AgreeDataBean data;

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public AgreeDataBean getData() {
        return data;
    }

    public static class AgreeDataBean {
        private AgreeInfoBean agree;

        public AgreeInfoBean getAgree() {
            return agree;
        }
    }

    public static class AgreeInfoBean {
        private String score;

        public String getScore() {
            return score;
        }
    }
}
