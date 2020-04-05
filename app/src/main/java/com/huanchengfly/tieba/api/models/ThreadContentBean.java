package com.huanchengfly.tieba.api.models;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.huanchengfly.tieba.api.adapters.PortraitAdapter;
import com.huanchengfly.tieba.api.adapters.SubPostListAdapter;
import com.huanchengfly.tieba.post.models.BaseBean;

import java.util.List;

public class ThreadContentBean extends BaseBean {
    @SerializedName("error_code")
    private String errorCode;
    @SerializedName("error_msg")
    private String errorMsg;
    @SerializedName("post_list")
    private List<PostListItemBean> postList;
    private PageInfoBean page;
    private UserInfoBean user;
    private ForumInfoBean forum;
    @SerializedName("display_forum")
    private ForumInfoBean displayForum;
    @SerializedName("has_floor")
    private String hasFloor;
    @SerializedName("is_new_url")
    private String isNewUrl;
    @SerializedName("user_list")
    private List<UserInfoBean> userList;
    private ThreadBean thread;
    private AntiInfoBean anti;

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public List<PostListItemBean> getPostList() {
        return postList;
    }

    public PageInfoBean getPage() {
        return page;
    }

    public UserInfoBean getUser() {
        return user;
    }

    public ForumInfoBean getForum() {
        return forum;
    }

    public ForumInfoBean getDisplayForum() {
        return displayForum;
    }

    public String getHasFloor() {
        return hasFloor;
    }

    public String getIsNewUrl() {
        return isNewUrl;
    }

    public List<UserInfoBean> getUserList() {
        return userList;
    }

    public ThreadBean getThread() {
        return thread;
    }

    public AntiInfoBean getAnti() {
        return anti;
    }

    public static class AntiInfoBean {
        private String tbs;

        public String getTbs() {
            return tbs;
        }
    }

    public static class ThreadInfoBean {
        @SerializedName("thread_id")
        private String threadId;
        @SerializedName("first_post_id")
        private String firstPostId;

        public String getThreadId() {
            return threadId;
        }

        public String getFirstPostId() {
            return firstPostId;
        }
    }

    public static class AgreeBean {
        @SerializedName("agree_num")
        private String agreeNum;
        @SerializedName("disagree_num")
        private String disagreeNum;
        @SerializedName("diff_agree_num")
        private String diffAgreeNum;
        @SerializedName("has_agree")
        private String hasAgree;

        public String getAgreeNum() {
            return agreeNum;
        }

        public String getDisagreeNum() {
            return disagreeNum;
        }

        public String getDiffAgreeNum() {
            return diffAgreeNum;
        }

        public String getHasAgree() {
            return hasAgree;
        }
    }

    public static class ThreadBean {
        private String id;
        private String title;
        @SerializedName("thread_info")
        private ThreadInfoBean threadInfo;
        private UserInfoBean author;
        @SerializedName("reply_num")
        private String replyNum;
        @SerializedName("collect_status")
        private String collectStatus;
        @SerializedName("agree_num")
        private String agreeNum;
        @SerializedName("post_id")
        private String postId;
        @SerializedName("thread_id")
        private String threadId;
        private AgreeBean agree;

        public String getThreadId() {
            return threadId;
        }

        public String getPostId() {
            return postId;
        }

        public ThreadInfoBean getThreadInfo() {
            return threadInfo;
        }

        public String getAgreeNum() {
            return agreeNum;
        }

        public AgreeBean getAgree() {
            return agree;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public UserInfoBean getAuthor() {
            return author;
        }

        public String getReplyNum() {
            return replyNum;
        }

        public String getCollectStatus() {
            return collectStatus;
        }
    }

    public static class UserInfoBean {
        @SerializedName("is_login")
        private String isLogin;
        private String id;
        private String name;
        @SerializedName("name_show")
        private String nameShow;
        @JsonAdapter(PortraitAdapter.class)
        private String portrait;
        private String type;
        @SerializedName("level_id")
        private String levelId;
        @SerializedName("is_like")
        private String isLike;
        @SerializedName("is_manager")
        private String isManager;

        public String getIsLogin() {
            return isLogin;
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

        public String getType() {
            return type;
        }

        public String getLevelId() {
            return levelId;
        }

        public String getIsLike() {
            return isLike;
        }

        public String getIsManager() {
            return isManager;
        }
    }

    public static class ForumInfoBean extends BaseBean {
        private String id;
        private String name;
        @SerializedName("is_exists")
        private String isExists;
        private String avatar;
        @SerializedName("first_class")
        private String firstClass;
        @SerializedName("second_class")
        private String secondClass;
        @SerializedName("is_liked")
        private String isLiked;
        @SerializedName("is_brand_forum")
        private String isBrandForum;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getIsExists() {
            return isExists;
        }

        public String getAvatar() {
            return avatar;
        }

        public String getFirstClass() {
            return firstClass;
        }

        public String getSecondClass() {
            return secondClass;
        }

        public String getIsLiked() {
            return isLiked;
        }

        public String getIsBrandForum() {
            return isBrandForum;
        }
    }

    public static class PageInfoBean {
        private String offset;
        @SerializedName("current_page")
        private String currentPage;
        @SerializedName("total_page")
        private String totalPage;
        @SerializedName("has_more")
        private String hasMore;
        @SerializedName("has_prev")
        private String hasPrev;

        public String getOffset() {
            return offset;
        }

        public String getCurrentPage() {
            return currentPage;
        }

        public String getTotalPage() {
            return totalPage;
        }

        public String getHasMore() {
            return hasMore;
        }

        public String getHasPrev() {
            return hasPrev;
        }
    }

    public static class PostListItemBean {
        private String id;
        private String title;
        private String floor;
        private String time;
        private List<ContentBean> content;
        @SerializedName("author_id")
        private String authorId;
        private UserInfoBean author;
        @SerializedName("sub_post_number")
        private String subPostNumber;
        @SerializedName("sub_post_list")
        @JsonAdapter(SubPostListAdapter.class)
        private SubPostListBean subPostList;

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getFloor() {
            return floor;
        }

        public String getTime() {
            return time;
        }

        public List<ContentBean> getContent() {
            return content;
        }

        public String getAuthorId() {
            return authorId;
        }

        public UserInfoBean getAuthor() {
            return author;
        }

        public String getSubPostNumber() {
            return subPostNumber;
        }

        public SubPostListBean getSubPostList() {
            return subPostList;
        }
    }

    public static class SubPostListBean {
        private String pid;
        @SerializedName("sub_post_list")
        private List<PostListItemBean> subPostList;

        public String getPid() {
            return pid;
        }

        public List<PostListItemBean> getSubPostList() {
            return subPostList;
        }
    }

    public static class ContentBean {
        private String type;
        private String text;
        private String link;
        private String src;
        private String uid;
        @SerializedName("origin_src")
        private String originSrc;
        @SerializedName("cdn_src")
        private String cdnSrc;
        @SerializedName("cdn_src_active")
        private String cdnSrcActive;
        @SerializedName("big_cdn_src")
        private String bigCdnSrc;
        @SerializedName("during_time")
        private String duringTime;
        private String bsize;
        private String c;
        private String width;
        private String height;
        @SerializedName("is_long_pic")
        private String isLongPic;
        @SerializedName("voice_md5")
        private String voiceMD5;

        public String getCdnSrc() {
            return cdnSrc;
        }

        public String getCdnSrcActive() {
            return cdnSrcActive;
        }

        public String getBigCdnSrc() {
            return bigCdnSrc;
        }

        public String getUid() {
            return uid;
        }

        public String getVoiceMD5() {
            return voiceMD5;
        }

        public String getDuringTime() {
            return duringTime;
        }

        public String getIsLongPic() {
            return isLongPic;
        }

        public String getType() {
            return type;
        }

        public String getText() {
            return text;
        }

        public ContentBean setText(String text) {
            this.text = text;
            return this;
        }

        public String getLink() {
            return link;
        }

        public String getSrc() {
            return src;
        }

        public String getOriginSrc() {
            return originSrc;
        }

        public String getBsize() {
            return bsize;
        }

        public String getC() {
            return c;
        }

        public String getWidth() {
            return width;
        }

        public String getHeight() {
            return height;
        }
    }
}
