package com.huanchengfly.tieba.api.models;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.huanchengfly.tieba.api.adapters.PortraitAdapter;
import com.huanchengfly.tieba.post.models.BaseBean;

import java.util.List;

public class MessageListBean extends BaseBean {
    @SerializedName("error_code")
    private String errorCode;
    private long time;
    @SerializedName("reply_list")
    private List<MessageInfoBean> replyList;
    @SerializedName("at_list")
    private List<MessageInfoBean> atList;
    private PageInfoBean page;
    private MessageBean message;

    public int getErrorCode() {
        return Integer.valueOf(errorCode);
    }

    public long getTime() {
        return time;
    }

    public List<MessageInfoBean> getReplyList() {
        return replyList;
    }

    public List<MessageInfoBean> getAtList() {
        return atList;
    }

    public MessageBean getMessage() {
        return message;
    }

    public PageInfoBean getPage() {
        return page;
    }

    public class UserInfoBean {
        private String id;
        private String name;
        @SerializedName("name_show")
        private String nameShow;
        @JsonAdapter(PortraitAdapter.class)
        private String portrait;

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

    public class ReplyerInfoBean extends UserInfoBean {
        @SerializedName("is_friend")
        private String isFriend;
        @SerializedName("is_fans")
        private String isFans;

        public String getIsFriend() {
            return isFriend;
        }

        public String getIsFans() {
            return isFans;
        }
    }

    public class MessageInfoBean {
        @SerializedName("is_floor")
        private String isFloor;
        private String title;
        private String content;
        @SerializedName("quote_content")
        private String quoteContent;
        private ReplyerInfoBean replyer;
        @SerializedName("quote_user")
        private UserInfoBean quoteUser;
        @SerializedName("thread_id")
        private String threadId;
        @SerializedName("post_id")
        private String postId;
        private String time;
        @SerializedName("fname")
        private String forumName;
        @SerializedName("quote_pid")
        private String quotePid;
        @SerializedName("thread_type")
        private String threadType;
        private String unread;

        public String getUnread() {
            return unread;
        }

        public String getIsFloor() {
            return isFloor;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public String getQuoteContent() {
            return quoteContent;
        }

        public ReplyerInfoBean getReplyer() {
            return replyer;
        }

        public UserInfoBean getQuoteUser() {
            return quoteUser;
        }

        public String getThreadId() {
            return threadId;
        }

        public String getPostId() {
            return postId;
        }

        public String getTime() {
            return time;
        }

        public String getForumName() {
            return forumName;
        }

        public String getQuotePid() {
            return quotePid;
        }

        public String getThreadType() {
            return threadType;
        }
    }

    public class MessageBean {
        @SerializedName("replyme")
        private String replyMe;
        @SerializedName("atme")
        private String atMe;
        private String fans;
        private String recycle;
        @SerializedName("storethread")
        private String storeThread;

        public String getReplyMe() {
            return replyMe;
        }

        public String getAtMe() {
            return atMe;
        }

        public String getFans() {
            return fans;
        }

        public String getRecycle() {
            return recycle;
        }

        public String getStoreThread() {
            return storeThread;
        }
    }

    public class PageInfoBean {
        @SerializedName("current_page")
        private String currentPage;
        @SerializedName("has_more")
        private String hasMore;
        @SerializedName("has_prev")
        private String hasPrev;

        public String getHasMore() {
            return hasMore;
        }

        public String getHasPrev() {
            return hasPrev;
        }

        public String getCurrentPage() {
            return currentPage;
        }
    }
}
