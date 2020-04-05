package com.huanchengfly.tieba.post.models.database;

import org.litepal.crud.LitePalSupport;

public class Block extends LitePalSupport {
    public static final int CATEGORY_BLACK_LIST = 10;
    public static final int CATEGORY_WHITE_LIST = 11;
    public static final int TYPE_KEYWORD = 0;
    public static final int TYPE_USER = 1;

    private int id;
    private int category;
    private int type;
    private String username;
    private String uid;
    private String keywords;

    public String getUid() {
        return uid;
    }

    public Block setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public int getCategory() {
        return category;
    }

    public Block setCategory(int category) {
        this.category = category;
        return this;
    }

    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public Block setType(int type) {
        this.type = type;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public Block setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getKeywords() {
        return keywords;
    }

    public Block setKeywords(String keywords) {
        this.keywords = keywords;
        return this;
    }
}
