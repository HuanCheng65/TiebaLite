package com.huanchengfly.tieba.post.api.models;

import com.google.gson.annotations.SerializedName;
import com.huanchengfly.tieba.post.models.BaseBean;

import java.util.List;

public class UserLikeForumBean extends BaseBean {
    @SerializedName("error_code")
    private String errorCode;
    @SerializedName("error_msg")
    private String errorMsg;
    @SerializedName("has_more")
    private String hasMore;
    @SerializedName("forum_list")
    private ForumListBean forumList;
    @SerializedName("common_forum_list")
    private ForumListBean commonForumList;

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public String getHasMore() {
        return hasMore;
    }

    public ForumListBean getForumList() {
        return forumList;
    }

    public ForumListBean getCommonForumList() {
        return commonForumList;
    }

    public static class ForumListBean {
        @SerializedName("non-gconforum")
        private List<ForumBean> forumList;

        public List<ForumBean> getForumList() {
            return forumList;
        }
    }

    public static class ForumBean {
        private String id;
        private String name;
        @SerializedName("level_id")
        private String levelId;
        @SerializedName("favo_type")
        private String favoType;
        @SerializedName("level_name")
        private String levelName;
        @SerializedName("cur_score")
        private String curScore;
        @SerializedName("levelup_score")
        private String levelUpScore;
        private String avatar;
        private String slogan;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getLevelId() {
            return levelId;
        }

        public String getFavoType() {
            return favoType;
        }

        public String getLevelName() {
            return levelName;
        }

        public String getCurScore() {
            return curScore;
        }

        public String getLevelUpScore() {
            return levelUpScore;
        }

        public String getAvatar() {
            return avatar;
        }

        public String getSlogan() {
            return slogan;
        }
    }

}
