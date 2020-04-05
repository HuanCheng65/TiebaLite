package com.huanchengfly.tieba.api.models;

import com.google.gson.annotations.SerializedName;
import com.huanchengfly.tieba.post.models.ErrorBean;

import java.util.List;

public class PicToIdJsonBean extends ErrorBean {
    private List<PicBean> pics;

    public List<PicBean> getPics() {
        return pics;
    }

    public static class PicBean {
        @SerializedName("pic_id")
        private String picId;
        private String width;
        private String height;

        public String getPicId() {
            return picId;
        }

        public String getWidth() {
            return width;
        }

        public String getHeight() {
            return height;
        }
    }
}
