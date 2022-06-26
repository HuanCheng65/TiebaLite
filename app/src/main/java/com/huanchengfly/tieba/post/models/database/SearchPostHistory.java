package com.huanchengfly.tieba.post.models.database;

import org.litepal.crud.LitePalSupport;

public class SearchPostHistory extends LitePalSupport {
    private int id;
    private long timestamp;
    private String content;
    private String forumName;

    public SearchPostHistory(String content, String forumName) {
        this.timestamp = System.currentTimeMillis();
        this.content = content;
        this.forumName = forumName;
    }

    public int getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getForumName() {
        return forumName;
    }

    public void setForumName(String forumName) {
        this.forumName = forumName;
    }
}
