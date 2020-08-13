package com.huanchengfly.tieba.api.models;

import com.google.gson.annotations.SerializedName;
import com.huanchengfly.tieba.post.models.BaseBean;

import java.util.List;

public class ThreadStoreBean extends BaseBean {
    @SerializedName("error_code")
    private String errorCode;
    private ErrorInfo error;
    @SerializedName("store_thread")
    private List<ThreadStoreInfo> storeThread;

    public String getErrorCode() {
        return errorCode;
    }

    public ErrorInfo getError() {
        return error;
    }

    public List<ThreadStoreInfo> getStoreThread() {
        return storeThread;
    }

    public class ThreadStoreInfo extends BaseBean {
        @SerializedName("thread_id")
        private String threadId;
        private String title;
        @SerializedName("forum_name")
        private String forumName;
        private AuthorInfo author;
        private List<MediaInfo> media;
        @SerializedName("is_deleted")
        private String isDeleted;
        @SerializedName("last_time")
        private String lastTime;
        private String type;
        private String status;
        @SerializedName("max_pid")
        private String maxPid;
        @SerializedName("min_pid")
        private String minPid;
        @SerializedName("mark_pid")
        private String markPid;
        @SerializedName("mark_status")
        private String markStatus;

        public String getThreadId() {
            return threadId;
        }

        public String getTitle() {
            return title;
        }

        public String getForumName() {
            return forumName;
        }

        public AuthorInfo getAuthor() {
            return author;
        }

        public List<MediaInfo> getMedia() {
            return media;
        }

        public String getIsDeleted() {
            return isDeleted;
        }

        public String getLastTime() {
            return lastTime;
        }

        public String getType() {
            return type;
        }

        public String getStatus() {
            return status;
        }

        public String getMaxPid() {
            return maxPid;
        }

        public String getMinPid() {
            return minPid;
        }

        public String getMarkPid() {
            return markPid;
        }

        public String getMarkStatus() {
            return markStatus;
        }
    }

    public class MediaInfo extends BaseBean {
        private String type;
        @SerializedName("small_Pic")
        private String smallPic;
        @SerializedName("big_pic")
        private String bigPic;
        private String width;
        private String height;

        public String getType() {
            return type;
        }

        public String getSmallPic() {
            return smallPic;
        }

        public String getBigPic() {
            return bigPic;
        }

        public String getWidth() {
            return width;
        }

        public String getHeight() {
            return height;
        }
    }

    public class AuthorInfo extends BaseBean {
        @SerializedName("lz_uid")
        private String lzUid;
        private String name;
        @SerializedName("name_show")
        private String nameShow;
        @SerializedName("user_portrait")
        private String userPortrait;

        public String getLzUid() {
            return lzUid;
        }

        public String getName() {
            return name;
        }

        public String getNameShow() {
            return nameShow;
        }

        public String getUserPortrait() {
            return userPortrait;
        }
    }

    public class ErrorInfo extends BaseBean {
        @SerializedName("errno")
        private String errorCode;
        @SerializedName("errmsg")
        private String errorMsg;

        public String getErrorCode() {
            return errorCode;
        }

        public String getErrorMsg() {
            return errorMsg;
        }
    }
}
