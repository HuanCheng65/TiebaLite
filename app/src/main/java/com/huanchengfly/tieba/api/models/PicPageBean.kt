package com.huanchengfly.tieba.api.models;

import com.google.gson.annotations.SerializedName;
import com.huanchengfly.tieba.post.models.BaseBean;

import java.util.List;

public class PicPageBean extends BaseBean {
    @SerializedName("error_code")
    private String errorCode;
    private ForumBean forum;
    @SerializedName("pic_amount")
    private String picAmount;
    @SerializedName("pic_list")
    private List<PicBean> picList;

    public String getErrorCode() {
        return errorCode;
    }

    public ForumBean getForum() {
        return forum;
    }

    public String getPicAmount() {
        return picAmount;
    }

    public List<PicBean> getPicList() {
        return picList;
    }

    public static class ForumBean {
        private String name;
        private String id;

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }
    }

    public static class PicBean {
        @SerializedName("overall_index")
        private String overAllIndex;
        private ImgBean img;
        @SerializedName("post_id")
        private String postId;
        @SerializedName("user_id")
        private String userId;
        @SerializedName("user_name")
        private String userName;

        public String getOverAllIndex() {
            return overAllIndex;
        }

        public ImgBean getImg() {
            return img;
        }

        public String getPostId() {
            return postId;
        }

        public String getUserId() {
            return userId;
        }

        public String getUserName() {
            return userName;
        }
    }

    public static class ImgBean {
        private ImgInfoBean original;
        private ImgInfoBean medium;
        private ImgInfoBean screen;

        public ImgInfoBean getOriginal() {
            return original;
        }

        public ImgInfoBean getMedium() {
            return medium;
        }

        public ImgInfoBean getScreen() {
            return screen;
        }
    }

    public static class ImgInfoBean {
        private String id;
        private String width;
        private String height;
        private String size;
        private String format;
        @SerializedName("waterurl")
        private String waterUrl;
        @SerializedName("big_cdn_src")
        private String bigCdnSrc;
        private String url;
        @SerializedName("original_src")
        private String originalSrc;

        public String getId() {
            return id;
        }

        public String getWidth() {
            return width;
        }

        public String getHeight() {
            return height;
        }

        public String getSize() {
            return size;
        }

        public String getFormat() {
            return format;
        }

        public String getWaterUrl() {
            return waterUrl;
        }

        public String getBigCdnSrc() {
            return bigCdnSrc;
        }

        public String getUrl() {
            return url;
        }

        public String getOriginalSrc() {
            return originalSrc;
        }
    }
}
