package com.huanchengfly.tieba.post.api.models;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.huanchengfly.tieba.post.api.adapters.PortraitAdapter;
import com.huanchengfly.tieba.post.api.adapters.UserPostContentAdapter;
import com.huanchengfly.tieba.post.models.BaseBean;

import java.util.ArrayList;
import java.util.List;

public class UserPostBean extends BaseBean {
    @SerializedName("error_code")
    private String errorCode;
    @SerializedName("error_msg")
    private String errorMsg;
    @SerializedName("hide_post")
    private String hidePost;
    @SerializedName("post_list")
    private List<PostBean> postList;

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public String getHidePost() {
        return hidePost;
    }

    public List<PostBean> getPostList() {
        return postList;
    }

    public static class PostBean {
        @SerializedName("forum_id")
        private String forumId;
        @SerializedName("thread_id")
        private String threadId;
        @SerializedName("post_id")
        private String postId;
        @SerializedName("is_thread")
        private String isThread;
        @SerializedName("create_time")
        private String createTime;
        @SerializedName("is_ntitle")
        private String isNoTitle;
        @SerializedName("forum_name")
        private String forumName;
        private String title;
        @SerializedName("user_name")
        private String userName;
        @SerializedName("is_post_deleted")
        private String isPostDeleted;
        @SerializedName("reply_num")
        private String replyNum;
        @SerializedName("freq_num")
        private String freqNum;
        @SerializedName("user_id")
        private String userId;
        @SerializedName("name_show")
        private String nameShow;
        @JsonAdapter(PortraitAdapter.class)
        @SerializedName("user_portrait")
        private String userPortrait;
        @SerializedName("post_type")
        private String postType;
        @JsonAdapter(UserPostContentAdapter.class)
        private List<ContentBean> content;
        @SerializedName("abstract")
        private List<PostContentBean> abstracts;

        public List<PostContentBean> getAbstracts() {
            return abstracts;
        }

        public String getPostType() {
            return postType;
        }

        public String getIsNoTitle() {
            return isNoTitle;
        }

        public List<ContentBean> getContent() {
            return content;
        }

        public String getForumId() {
            return forumId;
        }

        public String getThreadId() {
            return threadId;
        }

        public String getPostId() {
            return postId;
        }

        public String getIsThread() {
            return isThread;
        }

        public String getCreateTime() {
            return createTime;
        }

        public String getForumName() {
            return forumName;
        }

        public String getTitle() {
            return title;
        }

        public String getUserName() {
            return userName;
        }

        public String getIsPostDeleted() {
            return isPostDeleted;
        }

        public String getReplyNum() {
            return replyNum;
        }

        public String getFreqNum() {
            return freqNum;
        }

        public String getUserId() {
            return userId;
        }

        public String getNameShow() {
            return nameShow;
        }

        public String getUserPortrait() {
            return userPortrait;
        }
    }

    public static class ContentBean {
        @SerializedName("post_content")
        private List<PostContentBean> postContent;
        @SerializedName("create_time")
        private String createTime;
        @SerializedName("post_id")
        private String postId;

        public static ContentBean createContentBean(String content) {
            List<PostContentBean> list = new ArrayList<>();
            list.add(new PostContentBean()
                    .setType("0")
                    .setText(content));
            return new ContentBean()
                    .setPostContent(list)
                    .setCreateTime(null)
                    .setPostId(null);
        }

        public String getCreateTime() {
            return createTime;
        }

        public ContentBean setCreateTime(String createTime) {
            this.createTime = createTime;
            return this;
        }

        public String getPostId() {
            return postId;
        }

        public ContentBean setPostId(String postId) {
            this.postId = postId;
            return this;
        }

        public List<PostContentBean> getPostContent() {
            return postContent;
        }

        public ContentBean setPostContent(List<PostContentBean> postContent) {
            this.postContent = postContent;
            return this;
        }
    }

    public static class PostContentBean {
        private String type;
        private String text;

        public String getType() {
            return type;
        }

        public PostContentBean setType(String type) {
            this.type = type;
            return this;
        }

        public String getText() {
            return text;
        }

        public PostContentBean setText(String text) {
            this.text = text;
            return this;
        }
    }
}
