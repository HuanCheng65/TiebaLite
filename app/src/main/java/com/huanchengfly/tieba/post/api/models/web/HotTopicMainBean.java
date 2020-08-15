package com.huanchengfly.tieba.post.api.models.web;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class HotTopicMainBean extends WebBaseBean<HotTopicMainBean.HotTopicMainDataBean> {
    public static class HotTopicMainDataBean {
        @SerializedName("best_info")
        private BestInfoBean bestInfo;

        public BestInfoBean getBestInfo() {
            return bestInfo;
        }
    }

    public static class BestInfoBean {
        private List<BestInfoRetBean> ret;

        public List<BestInfoRetBean> getRet() {
            return ret;
        }
    }

    public static class BestInfoRetBean {
        @SerializedName("common_type")
        private String commonType;
        @SerializedName("module_name")
        private String moduleName;
        @SerializedName("module_recoms")
        private List<String> moduleRecoms;
        @SerializedName("thread_list")
        private Map<String, ThreadBean> threadList;
        @SerializedName("recom_type")
        private String recomType;
        @SerializedName("topic_id")
        private String topicId;

        public String getCommonType() {
            return commonType;
        }

        public String getModuleName() {
            return moduleName;
        }

        public Map<String, ThreadBean> getThreadList() {
            return threadList;
        }

        public List<String> getModuleRecoms() {
            return moduleRecoms;
        }

        public String getRecomType() {
            return recomType;
        }

        public String getTopicId() {
            return topicId;
        }
    }

    public static class ThreadBean {
        @SerializedName("abstract")
        private String abstracts;
        @SerializedName("agree_num")
        private String agreeNum;
        private String avatar;
        @SerializedName("create_time")
        private String createTime;
        @SerializedName("forum_id")
        private String forumId;
        @SerializedName("forum_name")
        private String forumName;
        private List<MediaBean> media;
        @SerializedName("name_show")
        private String nameShow;
        @SerializedName("post_num")
        private String postNum;
        @SerializedName("thread_id")
        private String threadId;
        @SerializedName("user_id")
        private String userId;
        private String title;

        public String getAbstracts() {
            return abstracts;
        }

        public String getAgreeNum() {
            return agreeNum;
        }

        public String getAvatar() {
            return avatar;
        }

        public String getCreateTime() {
            return createTime;
        }

        public String getForumId() {
            return forumId;
        }

        public String getForumName() {
            return forumName;
        }

        public List<MediaBean> getMedia() {
            return media;
        }

        public String getNameShow() {
            return nameShow;
        }

        public String getPostNum() {
            return postNum;
        }

        public String getThreadId() {
            return threadId;
        }

        public String getUserId() {
            return userId;
        }

        public String getTitle() {
            return title;
        }
    }

    public static class MediaBean {
        @SerializedName("big_pic")
        private String bigPic;
        private int height;
        private int width;
        @SerializedName("small_pic")
        private String smallPic;
        private String type;
        @SerializedName("water_pic")
        private String waterPic;

        public String getBigPic() {
            return bigPic;
        }

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }

        public String getSmallPic() {
            return smallPic;
        }

        public String getType() {
            return type;
        }

        public String getWaterPic() {
            return waterPic;
        }
    }

}
