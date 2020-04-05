package com.huanchengfly.tieba.post.models.database;

import org.litepal.crud.LitePalSupport;

public class SearchHistory extends LitePalSupport {
    private int id;
    private long timestamp;
    private String content;

    public SearchHistory(String content) {
        this.timestamp = System.currentTimeMillis();
        this.content = content;
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
}
