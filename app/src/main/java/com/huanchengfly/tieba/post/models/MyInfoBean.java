package com.huanchengfly.tieba.post.models;

import com.google.gson.annotations.SerializedName;
import com.huanchengfly.tieba.post.api.models.web.WebBaseBean;

public class MyInfoBean extends WebBaseBean<MyInfoBean.MyInfoDataBean> {
    public static class MyInfoDataBean {
        @SerializedName("itb_tbs")
        public String itbTbs;
        public String tbs;
        @SerializedName("portrait_url")
        public String avatarUrl;
        public long uid;
        @SerializedName("user_sex")
        public int userSex;
        @SerializedName("name_show")
        public String showName;
        public String intro;
        public String name;
        @SerializedName("concern_num")
        public String concernNum;
        @SerializedName("fans_num")
        public String fansNum;
        @SerializedName("like_forum_num")
        public String likeForumNum;
        @SerializedName("post_num")
        public String postNum;
        @SerializedName("is_login")
        public boolean isLogin;

        public String getIntro() {
            return intro;
        }

        public MyInfoDataBean setIntro(String intro) {
            this.intro = intro;
            return this;
        }

        public String getItbTbs() {
            return itbTbs;
        }

        public String getTbs() {
            return tbs;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public long getUid() {
            return uid;
        }

        public int getUserSex() {
            return userSex;
        }

        public String getShowName() {
            return showName;
        }

        public String getName() {
            return name;
        }

        public String getConcernNum() {
            return concernNum;
        }

        public String getFansNum() {
            return fansNum;
        }

        public String getLikeForumNum() {
            return likeForumNum;
        }

        public String getPostNum() {
            return postNum;
        }

        public boolean isLogin() {
            return isLogin;
        }
    }
}