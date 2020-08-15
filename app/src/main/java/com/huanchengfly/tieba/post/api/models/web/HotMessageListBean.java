package com.huanchengfly.tieba.post.api.models.web;

import com.google.gson.annotations.SerializedName;
import com.huanchengfly.tieba.post.models.BaseBean;

import java.util.List;

public class HotMessageListBean extends BaseBean {
    @SerializedName("no")
    private int errorCode;
    @SerializedName("error")
    private String errorMsg;
    private HotMessageListDataBean data;

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public HotMessageListDataBean getData() {
        return data;
    }

    public static class HotMessageListDataBean {
        private DataListBean list;

        public DataListBean getList() {
            return list;
        }
    }

    public static class DataListBean {
        private List<HotMessageRetBean> ret;

        public List<HotMessageRetBean> getRet() {
            return ret;
        }
    }

    public static class HotMessageRetBean {
        @SerializedName("mul_id")
        private String mulId;
        @SerializedName("mul_name")
        private String mulName;
        @SerializedName("topic_info")
        private TopicInfoBean topicInfo;

        public TopicInfoBean getTopicInfo() {
            return topicInfo;
        }

        public String getMulId() {
            return mulId;
        }

        public String getMulName() {
            return mulName;
        }
    }

    public static class TopicInfoBean {
        @SerializedName("topic_desc")
        private String topicDesc;

        public String getTopicDesc() {
            return topicDesc;
        }
    }
}
