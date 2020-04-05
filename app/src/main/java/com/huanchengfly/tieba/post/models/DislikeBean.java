package com.huanchengfly.tieba.post.models;

import com.google.gson.annotations.SerializedName;

public class DislikeBean extends BaseBean {
    @SerializedName("tid")
    private String threadId;
    @SerializedName("dislike_ids")
    private String dislikeIds;
    @SerializedName("fid")
    private String forumId;
    @SerializedName("click_time")
    private long clickTime;
    private String extra;

    public DislikeBean(String threadId, String dislikeIds, String forumId, long clickTime, String extra) {
        this.threadId = threadId;
        this.dislikeIds = dislikeIds;
        this.forumId = forumId;
        this.clickTime = clickTime;
        this.extra = extra;
    }

    public String getThreadId() {
        return threadId;
    }

    public DislikeBean setThreadId(String threadId) {
        this.threadId = threadId;
        return this;
    }

    public String getDislikeIds() {
        return dislikeIds;
    }

    public DislikeBean setDislikeIds(String dislikeIds) {
        this.dislikeIds = dislikeIds;
        return this;
    }

    public String getForumId() {
        return forumId;
    }

    public DislikeBean setForumId(String forumId) {
        this.forumId = forumId;
        return this;
    }

    public long getClickTime() {
        return clickTime;
    }

    public DislikeBean setClickTime(long clickTime) {
        this.clickTime = clickTime;
        return this;
    }

    public String getExtra() {
        return extra;
    }

    public DislikeBean setExtra(String extra) {
        this.extra = extra;
        return this;
    }
}
