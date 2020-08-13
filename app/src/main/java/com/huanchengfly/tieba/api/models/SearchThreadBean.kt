package com.huanchengfly.tieba.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchThreadBean {
    @SerializedName("no")
    private int errorCode;
    @SerializedName("error")
    private String errorMsg;
    private DataBean data;

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public DataBean getData() {
        return data;
    }

    public class DataBean {
        @SerializedName("has_more")
        private int hasMore;
        @SerializedName("current_page")
        private int currentPage;
        @SerializedName("post_list")
        private List<ThreadInfoBean> postList;

        public int getHasMore() {
            return hasMore;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public List<ThreadInfoBean> getPostList() {
            return postList;
        }
    }

    public class ThreadInfoBean {
        private String tid;
        private String pid;
        private String title;
        private String content;
        private String time;
        @SerializedName("post_num")
        private String postNum;
        @SerializedName("forum_name")
        private String forumName;
        private UserInfoBean user;
        private int type;

        public String getForumName() {
            return forumName;
        }

        public String getTid() {
            return tid;
        }

        public String getPid() {
            return pid;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public String getTime() {
            return time;
        }

        public String getPostNum() {
            return postNum;
        }

        public UserInfoBean getUser() {
            return user;
        }

        public int getType() {
            return type;
        }
    }

    public class UserInfoBean {
        @SerializedName("user_name")
        private String userName;
        @SerializedName("user_id")
        private String userId;
        private String portrait;

        public String getUserName() {
            return userName;
        }

        public String getUserId() {
            return userId;
        }

        public String getPortrait() {
            return portrait;
        }
    }
}
