package com.huanchengfly.tieba.api.models;

import com.google.gson.annotations.SerializedName;
import com.huanchengfly.tieba.post.models.BaseBean;

public class ProfileBean extends BaseBean {
    @SerializedName("error_code")
    private String errorCode;
    @SerializedName("error_msg")
    private String errorMsg;
    private AntiBean anti;
    private UserBean user;

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public AntiBean getAnti() {
        return anti;
    }

    public UserBean getUser() {
        return user;
    }

    public static class AntiBean {
        private String tbs;

        public String getTbs() {
            return tbs;
        }
    }

    public static class UserBean {
        private String id;
        private String name;
        @SerializedName("name_show")
        private String nameShow;
        private String portrait;
        private String intro;
        private String sex;
        @SerializedName("post_num")
        private String postNum;
        @SerializedName("repost_num")
        private String repostNum;
        @SerializedName("thread_num")
        private String threadNum;
        @SerializedName("tb_age")
        private String tbAge;
        @SerializedName("my_like_num")
        private String myLikeNum;
        @SerializedName("like_forum_num")
        private String likeForumNum;
        @SerializedName("concern_num")
        private String concernNum;
        @SerializedName("fans_num")
        private String fansNum;
        @SerializedName("has_concerned")
        private String hasConcerned;
        @SerializedName("is_fans")
        private String isFans;

        public String getIntro() {
            return intro;
        }

        public String getSex() {
            return sex;
        }

        public String getPostNum() {
            return postNum;
        }

        public String getRepostNum() {
            return repostNum;
        }

        public String getThreadNum() {
            return threadNum;
        }

        public String getTbAge() {
            return tbAge;
        }

        public String getMyLikeNum() {
            return myLikeNum;
        }

        public String getLikeForumNum() {
            return likeForumNum;
        }

        public String getConcernNum() {
            return concernNum;
        }

        public String getFansNum() {
            return fansNum;
        }

        public String getHasConcerned() {
            return hasConcerned;
        }

        public UserBean setHasConcerned(String hasConcerned) {
            this.hasConcerned = hasConcerned;
            return this;
        }

        public String getIsFans() {
            return isFans;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getNameShow() {
            return nameShow;
        }

        public String getPortrait() {
            return portrait;
        }
    }
}
