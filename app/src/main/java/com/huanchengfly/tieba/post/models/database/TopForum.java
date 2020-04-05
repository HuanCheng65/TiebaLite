package com.huanchengfly.tieba.post.models.database;

import org.litepal.crud.LitePalSupport;

public class TopForum extends LitePalSupport {
    private String forumId;
    private int id;

    public TopForum(String forumId) {
        this.forumId = forumId;
    }

    public int getId() {
        return id;
    }

    public String getForumId() {
        return forumId;
    }

    public TopForum setForumId(String forumId) {
        this.forumId = forumId;
        return this;
    }
}
