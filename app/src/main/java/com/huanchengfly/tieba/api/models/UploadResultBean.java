package com.huanchengfly.tieba.api.models;

import com.google.gson.annotations.SerializedName;
import com.huanchengfly.utils.GsonUtil;

public class UploadResultBean {
    @SerializedName("error_code")
    private int errorCode;
    @SerializedName("error_msg")
    private String errorMsg;
    private UploadInfo info;

    public String getErrorMsg() {
        return errorMsg;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public UploadInfo getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return GsonUtil.getGson().toJson(this);
    }

    public class UploadInfo {
        @SerializedName("pic_id")
        private String picId;
        private String width;
        private String height;
        @SerializedName("pic_url")
        private String picUrl;

        public String getPicId() {
            return picId;
        }

        public UploadInfo setPicId(String picId) {
            this.picId = picId;
            return this;
        }

        public String getWidth() {
            return width;
        }

        public UploadInfo setWidth(String width) {
            this.width = width;
            return this;
        }

        public String getHeight() {
            return height;
        }

        public UploadInfo setHeight(String height) {
            this.height = height;
            return this;
        }

        public String getPicUrl() {
            return picUrl;
        }

        public String getPic() {
            return "#(pic," + getPicId() +
                    "," +
                    getWidth() +
                    "," +
                    getHeight() +
                    ")\n";
        }
    }
}
