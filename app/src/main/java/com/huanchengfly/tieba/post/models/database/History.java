package com.huanchengfly.tieba.post.models.database;

import org.litepal.crud.LitePalSupport;

public class History extends LitePalSupport {
    private int id;
    private String avatar;
    private String username;
    private String data;
    private String extras;
    private String title;
    private long timestamp;
    private int count;
    private int type;

    public String getUsername() {
        return username;
    }

    public History setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getAvatar() {
        return avatar;
    }

    public History setAvatar(String avatar) {
        this.avatar = avatar;
        return this;
    }

    public String getExtras() {
        return extras;
    }

    public History setExtras(String extras) {
        this.extras = extras;
        return this;
    }

    public int getId() {
        return id;
    }

    public String getData() {
        return data;
    }

    public History setData(String data) {
        this.data = data;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public History setTitle(String title) {
        this.title = title;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public History setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public int getCount() {
        return count;
    }

    public History setCount(int count) {
        this.count = count;
        return this;
    }

    public int getType() {
        return type;
    }

    public History setType(int type) {
        this.type = type;
        return this;
    }
}
