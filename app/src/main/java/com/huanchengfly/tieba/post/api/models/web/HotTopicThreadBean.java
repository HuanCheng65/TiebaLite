package com.huanchengfly.tieba.post.api.models.web;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HotTopicThreadBean extends WebBaseBean<HotTopicThreadBean.HotTopicThreadDataBean> {
    public static class HotTopicThreadDataBean {
        @SerializedName("thread_list")
        private List<HotTopicMainBean.ThreadBean> threadList;

        public List<HotTopicMainBean.ThreadBean> getThreadList() {
            return threadList;
        }
    }
}
