package com.huanchengfly.tieba.post.models.database;

import org.litepal.crud.LitePalSupport;

public class Draft extends LitePalSupport {
    private String hash;
    private String content;

    public Draft(String hash, String content) {
        this.hash = hash;
        this.content = content;
    }

    public String getHash() {
        return hash;
    }

    public Draft setHash(String hash) {
        this.hash = hash;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Draft setContent(String content) {
        this.content = content;
        return this;
    }
}
