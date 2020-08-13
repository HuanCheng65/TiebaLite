package com.huanchengfly.tieba.api.models;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.huanchengfly.tieba.api.adapters.ExactMatchAdapter;
import com.huanchengfly.tieba.api.adapters.ForumFuzzyMatchAdapter;
import com.huanchengfly.tieba.post.models.BaseBean;

import java.util.List;

public class SearchForumBean extends BaseBean {
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

    public static class ExactForumInfoBean extends ForumInfoBean {
        private String intro;
        private String slogan;
        @SerializedName("is_jiucuo")
        private int isJiucuo;

        public String getIntro() {
            return intro;
        }

        public String getSlogan() {
            return slogan;
        }

        public int getIsJiucuo() {
            return isJiucuo;
        }
    }

    public static class ForumInfoBean {
        @SerializedName("forum_id")
        private int forumId;
        @SerializedName("forum_name")
        private String forumName;
        @SerializedName("forum_name_show")
        private String forumNameShow;
        private String avatar;
        @SerializedName("post_num")
        private String postNum;
        @SerializedName("concern_num")
        private String concernNum;
        @SerializedName("has_concerned")
        private int hasConcerned;

        public int getForumId() {
            return forumId;
        }

        public ForumInfoBean setForumId(int forumId) {
            this.forumId = forumId;
            return this;
        }

        public String getForumName() {
            return forumName;
        }

        public ForumInfoBean setForumName(String forumName) {
            this.forumName = forumName;
            return this;
        }

        public String getForumNameShow() {
            return forumNameShow;
        }

        public ForumInfoBean setForumNameShow(String forumNameShow) {
            this.forumNameShow = forumNameShow;
            return this;
        }

        public String getAvatar() {
            return avatar;
        }

        public ForumInfoBean setAvatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public String getPostNum() {
            return postNum;
        }

        public ForumInfoBean setPostNum(String postNum) {
            this.postNum = postNum;
            return this;
        }

        public String getConcernNum() {
            return concernNum;
        }

        public ForumInfoBean setConcernNum(String concernNum) {
            this.concernNum = concernNum;
            return this;
        }

        public int getHasConcerned() {
            return hasConcerned;
        }

        public ForumInfoBean setHasConcerned(int hasConcerned) {
            this.hasConcerned = hasConcerned;
            return this;
        }
    }

    public class DataBean {
        @SerializedName("has_more")
        private int hasMore;
        @SerializedName("pn")
        private int page;
        @JsonAdapter(ForumFuzzyMatchAdapter.class)
        private List<ForumInfoBean> fuzzyMatch;
        @JsonAdapter(ExactMatchAdapter.class)
        private ExactForumInfoBean exactMatch;

        public int getHasMore() {
            return hasMore;
        }

        public int getPage() {
            return page;
        }

        public List<ForumInfoBean> getFuzzyMatch() {
            return fuzzyMatch;
        }

        public ExactForumInfoBean getExactMatch() {
            return exactMatch;
        }
    }
}
