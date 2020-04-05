package com.huanchengfly.tieba.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NewUpdateBean {
    private boolean success;
    @SerializedName("has_update")
    private boolean hasUpdate;
    @SerializedName("error_code")
    private int errorCode;
    @SerializedName("error_message")
    private String errorMsg;
    private ResultBean result;

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isHasUpdate() {
        return hasUpdate;
    }

    public ResultBean getResult() {
        return result;
    }

    public static class ResultBean {
        private boolean cancelable;
        @SerializedName("update_content")
        private List<String> updateContent;
        @SerializedName("version_code")
        private int versionCode;
        @SerializedName("version_name")
        private String versionName;
        @SerializedName("version_type")
        private int versionType;
        private List<DownloadBean> downloads;

        public List<DownloadBean> getDownloads() {
            return downloads;
        }

        public boolean isCancelable() {
            return cancelable;
        }

        public List<String> getUpdateContent() {
            return updateContent;
        }

        public int getVersionCode() {
            return versionCode;
        }

        public String getVersionName() {
            return versionName;
        }

        public int getVersionType() {
            return versionType;
        }
    }

    public static class DownloadBean {
        private String name;
        private String url;

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }
    }
}
