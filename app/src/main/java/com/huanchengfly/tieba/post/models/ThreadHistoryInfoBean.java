package com.huanchengfly.tieba.post.models;

public class ThreadHistoryInfoBean extends BaseBean {
    private boolean seeLz;
    private String pid;
    private String forumName;
    private String floor;

    public String getForumName() {
        return forumName;
    }

    public ThreadHistoryInfoBean setForumName(String forumName) {
        this.forumName = forumName;
        return this;
    }

    public String getFloor() {
        return floor;
    }

    public ThreadHistoryInfoBean setFloor(String floor) {
        this.floor = floor;
        return this;
    }

    public boolean isSeeLz() {
        return seeLz;
    }

    public ThreadHistoryInfoBean setSeeLz(boolean seeLz) {
        this.seeLz = seeLz;
        return this;
    }

    public String getPid() {
        return pid;
    }

    public ThreadHistoryInfoBean setPid(String pid) {
        this.pid = pid;
        return this;
    }
}