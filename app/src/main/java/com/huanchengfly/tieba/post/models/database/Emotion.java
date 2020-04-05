package com.huanchengfly.tieba.post.models.database;

import org.litepal.crud.LitePalSupport;

public class Emotion extends LitePalSupport {
    private int id;
    private String picId;
    private String size;

    public int getId() {
        return id;
    }

    public Emotion setId(int id) {
        this.id = id;
        return this;
    }

    public String getPicId() {
        return picId;
    }

    public Emotion setPicId(String picId) {
        this.picId = picId;
        return this;
    }

    public String getSize() {
        return size;
    }

    public Emotion setSize(String size) {
        this.size = size;
        return this;
    }
}
