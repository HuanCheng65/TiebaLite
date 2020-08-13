package com.huanchengfly.tieba.api.models;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.huanchengfly.tieba.api.adapters.MediaAdapter;
import com.huanchengfly.tieba.api.adapters.PortraitAdapter;
import com.huanchengfly.tieba.api.models.ForumPageBean.AbstractBean;
import com.huanchengfly.tieba.api.models.ForumPageBean.MediaInfoBean;
import com.huanchengfly.tieba.api.models.ForumPageBean.VideoInfoBean;

import java.util.List;

public class PersonalizedBean {
    @SerializedName("error_code")
    private String errorCode;
    @SerializedName("error_msg")
    private String errorMsg;
    @SerializedName("thread_list")
    private List<ThreadBean> threadList;
    @SerializedName("thread_personalized")
    private List<ThreadPersonalizedBean> threadPersonalized;

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public List<ThreadBean> getThreadList() {
        return threadList;
    }

    public List<ThreadPersonalizedBean> getThreadPersonalized() {
        return threadPersonalized;
    }

    public static class ThreadPersonalizedBean {
        private String tid;
        @SerializedName("dislike_resource")
        private List<DislikeResourceBean> dislikeResource;

        public String getTid() {
            return tid;
        }

        public List<DislikeResourceBean> getDislikeResource() {
            return dislikeResource;
        }
    }

    public static class DislikeResourceBean {
        private String extra;
        @SerializedName("dislike_id")
        private String dislikeId;
        @SerializedName("dislike_reason")
        private String dislikeReason;

        public String getExtra() {
            return extra;
        }

        public String getDislikeId() {
            return dislikeId;
        }

        public String getDislikeReason() {
            return dislikeReason;
        }
    }

    public static class ThreadBean {
        private String id;
        private String tid;
        private String title;
        private AuthorBean author;
        @SerializedName("reply_num")
        private String replyNum;
        @SerializedName("view_num")
        private String viewNum;
        @SerializedName("last_time")
        private String lastTime;
        @SerializedName("last_time_int")
        private String lastTimeInt;
        @SerializedName("agree_num")
        private String agreeNum;
        @SerializedName("is_top")
        private String isTop;
        @SerializedName("is_good")
        private String isGood;
        @SerializedName("is_ntitle")
        private String isNoTitle;
        @SerializedName("fid")
        private String forumId;
        @SerializedName("fname")
        private String forumName;
        /*
        @SerializedName("media_num")
        private MediaNumBean mediaNum;
        */
        @SerializedName("video_info")
        private VideoInfoBean videoInfo;
        @JsonAdapter(MediaAdapter.class)
        private List<MediaInfoBean> media;
        @SerializedName("abstract")
        private List<AbstractBean> abstractBeans;

        private ThreadPersonalizedBean threadPersonalizedBean;

        public ThreadPersonalizedBean getThreadPersonalizedBean() {
            return threadPersonalizedBean;
        }

        public void setThreadPersonalizedBean(ThreadPersonalizedBean threadPersonalizedBean) {
            this.threadPersonalizedBean = threadPersonalizedBean;
        }

        public String getForumId() {
            return forumId;
        }

        public AuthorBean getAuthor() {
            return author;
        }

        public String getForumName() {
            return forumName;
        }

        public String getId() {
            return id;
        }

        public String getTid() {
            return tid;
        }

        public String getTitle() {
            return title;
        }

        public String getReplyNum() {
            return replyNum;
        }

        public String getViewNum() {
            return viewNum;
        }

        public String getLastTime() {
            return lastTime;
        }

        public String getLastTimeInt() {
            return lastTimeInt;
        }

        public String getAgreeNum() {
            return agreeNum;
        }

        public String getIsTop() {
            return isTop;
        }

        public String getIsGood() {
            return isGood;
        }

        public String getIsNoTitle() {
            return isNoTitle;
        }

        /*public MediaNumBean getMediaNum() {
            return mediaNum;
        }*/

        public VideoInfoBean getVideoInfo() {
            return videoInfo;
        }

        public List<MediaInfoBean> getMedia() {
            return media;
        }

        public List<AbstractBean> getAbstractBeans() {
            return abstractBeans;
        }
    }

    public static class AuthorBean {
        private String id;
        private String name;
        @SerializedName("name_show")
        private String nameShow;
        @JsonAdapter(PortraitAdapter.class)
        private String portrait;
        @SerializedName("has_concerned")
        private String hasConcerned;

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

        public String getHasConcerned() {
            return hasConcerned;
        }
    }

    public static class MediaNumBean {
        private String pic;

        public String getPic() {
            return pic;
        }
    }
}
