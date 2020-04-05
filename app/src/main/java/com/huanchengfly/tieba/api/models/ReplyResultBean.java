package com.huanchengfly.tieba.api.models;

import com.google.gson.annotations.SerializedName;

public class ReplyResultBean {
    @SerializedName("error_code")
    public String errorCode;
    @SerializedName("error_msg")
    public String errorMsg;
    public InfoBean info;
    private String pid;

    public String getPid() {
        return pid;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public InfoBean getInfo() {
        return info;
    }

    public class InfoBean {
        @SerializedName("need_vcode")
        private String needVcode;
        @SerializedName("vcode_md5")
        private String vcodeMD5;
        @SerializedName("vcode_pic_url")
        private String vcodePicUrl;
        @SerializedName("pass_token")
        private String passToken;

        public String getPassToken() {
            return passToken;
        }

        public String getNeedVcode() {
            return needVcode;
        }

        public String getVcodeMD5() {
            return vcodeMD5;
        }

        public String getVcodePicUrl() {
            return vcodePicUrl;
        }
    }
}