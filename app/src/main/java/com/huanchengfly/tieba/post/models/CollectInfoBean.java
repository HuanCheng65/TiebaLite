package com.huanchengfly.tieba.post.models;

public class CollectInfoBean {
    private String title;
    private String subtitle;
    private long tid;
    private boolean hasUpdate;
    private String url;

    public String getTitle() {
        return title;
    }

    public CollectInfoBean setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public CollectInfoBean setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public long getTid() {
        return tid;
    }

    public CollectInfoBean setTid(long tid) {
        this.tid = tid;
        return this;
    }

    public boolean isHasUpdate() {
        return hasUpdate;
    }

    public CollectInfoBean setHasUpdate(boolean hasUpdate) {
        this.hasUpdate = hasUpdate;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public CollectInfoBean setUrl(String url) {
        this.url = url;
        return this;
    }
}
