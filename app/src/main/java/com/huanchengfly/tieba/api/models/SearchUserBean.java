package com.huanchengfly.tieba.api.models;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.huanchengfly.tieba.api.adapters.ExactMatchAdapter;
import com.huanchengfly.tieba.api.adapters.UserFuzzyMatchAdapter;
import com.huanchengfly.tieba.post.models.BaseBean;

import java.util.List;

public class SearchUserBean extends BaseBean {
    @SerializedName("no")
    private int errorCode;
    @SerializedName("error")
    private String errorMsg;
    private SearchUserDataBean data;

    public SearchUserDataBean getData() {
        return data;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public static class SearchUserDataBean {
        @SerializedName("pn")
        private int pageNum;
        @SerializedName("has_more")
        private int hasMore;
        @JsonAdapter(ExactMatchAdapter.class)
        private UserBean exactMatch;
        @JsonAdapter(UserFuzzyMatchAdapter.class)
        private List<UserBean> fuzzyMatch;

        public int getPageNum() {
            return pageNum;
        }

        public int getHasMore() {
            return hasMore;
        }

        public UserBean getExactMatch() {
            return exactMatch;
        }

        public List<UserBean> getFuzzyMatch() {
            return fuzzyMatch;
        }
    }

    public static class UserBean {
        private String id;
        private String intro;
        @SerializedName("user_nickname")
        private String userNickname;
        private String name;
        private String portrait;
        @SerializedName("fans_num")
        private String fansNum;
        @SerializedName("has_concerned")
        private int hasConcerned;

        public String getId() {
            return id;
        }

        public UserBean setId(String id) {
            this.id = id;
            return this;
        }

        public String getIntro() {
            return intro;
        }

        public UserBean setIntro(String intro) {
            this.intro = intro;
            return this;
        }

        public String getUserNickname() {
            return userNickname;
        }

        public UserBean setUserNickname(String userNickname) {
            this.userNickname = userNickname;
            return this;
        }

        public String getName() {
            return name;
        }

        public UserBean setName(String name) {
            this.name = name;
            return this;
        }

        public String getPortrait() {
            return portrait;
        }

        public UserBean setPortrait(String portrait) {
            this.portrait = portrait;
            return this;
        }

        public String getFansNum() {
            return fansNum;
        }

        public UserBean setFansNum(String fansNum) {
            this.fansNum = fansNum;
            return this;
        }

        public int getHasConcerned() {
            return hasConcerned;
        }

        public UserBean setHasConcerned(int hasConcerned) {
            this.hasConcerned = hasConcerned;
            return this;
        }
    }
}
