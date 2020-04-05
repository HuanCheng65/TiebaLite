package com.huanchengfly.tieba.post.models;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.huanchengfly.utils.GsonUtil;

import java.util.Objects;

public class ReplyInfoBean {
    private String threadId;
    private String forumId;
    private String forumName;
    private String tbs;
    private String pid;
    private String spid;
    private String floorNum;
    private String replyUser;
    private String nickName;
    private String pn;
    private boolean isSubFloor;

    public ReplyInfoBean(String threadId, String forumId, String forumName, String tbs, String pid, String spid, String floorNum, String replyUser, String nickName) {
        this.threadId = threadId;
        this.forumId = forumId;
        this.forumName = forumName;
        this.tbs = tbs;
        this.pid = pid;
        this.spid = spid;
        this.floorNum = floorNum;
        this.replyUser = replyUser;
        this.nickName = nickName;
        this.isSubFloor = !TextUtils.equals(pid, spid);
    }

    public ReplyInfoBean(String threadId, String forumId, String forumName, String tbs, String pid, String floorNum, String replyUser, String nickName) {
        this.threadId = threadId;
        this.forumId = forumId;
        this.forumName = forumName;
        this.tbs = tbs;
        this.pid = pid;
        this.spid = null;
        this.floorNum = floorNum;
        this.replyUser = replyUser;
        this.nickName = nickName;
        this.isSubFloor = false;
    }

    public ReplyInfoBean(String threadId, String forumId, String forumName, String tbs, String nickName) {
        this.threadId = threadId;
        this.forumId = forumId;
        this.forumName = forumName;
        this.tbs = tbs;
        this.pid = null;
        this.spid = null;
        this.nickName = nickName;
        this.isSubFloor = false;
    }

    public String hash() {
        return getThreadId() + "-" + getPid() + "-" + getSpid();
    }

    public String getPn() {
        return pn;
    }

    public ReplyInfoBean setPn(String pn) {
        this.pn = pn;
        return this;
    }

    public String getSpid() {
        return spid;
    }

    public ReplyInfoBean setSpid(String spid) {
        this.spid = spid;
        return this;
    }

    public String getNickName() {
        return nickName;
    }

    public ReplyInfoBean setNickName(String nickName) {
        this.nickName = nickName;
        return this;
    }

    public boolean isSubFloor() {
        return isSubFloor;
    }

    public ReplyInfoBean setSubFloor(boolean subFloor) {
        isSubFloor = subFloor;
        return this;
    }

    public String getReplyUser() {
        return replyUser;
    }

    public ReplyInfoBean setReplyUser(String replyUser) {
        this.replyUser = replyUser;
        return this;
    }

    @NonNull
    @Override
    public String toString() {
        return GsonUtil.getGson().toJson(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getThreadId(), getForumId(), getPid());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReplyInfoBean)) return false;
        ReplyInfoBean that = (ReplyInfoBean) o;
        return Objects.equals(getThreadId(), that.getThreadId()) &&
                Objects.equals(getForumId(), that.getForumId()) &&
                Objects.equals(getPid(), that.getPid());
    }

    public String getThreadId() {
        return threadId;
    }

    public ReplyInfoBean setThreadId(String threadId) {
        this.threadId = threadId;
        return this;
    }

    public String getForumId() {
        return forumId;
    }

    public ReplyInfoBean setForumId(String forumId) {
        this.forumId = forumId;
        return this;
    }

    public String getForumName() {
        return forumName;
    }

    public ReplyInfoBean setForumName(String forumName) {
        this.forumName = forumName;
        return this;
    }

    public String getTbs() {
        return tbs;
    }

    public ReplyInfoBean setTbs(String tbs) {
        this.tbs = tbs;
        return this;
    }

    public String getPid() {
        return pid;
    }

    public ReplyInfoBean setPid(String pid) {
        this.pid = pid;
        return this;
    }

    public String getFloorNum() {
        return floorNum;
    }

    public ReplyInfoBean setFloorNum(String floorNum) {
        this.floorNum = floorNum;
        return this;
    }
}
