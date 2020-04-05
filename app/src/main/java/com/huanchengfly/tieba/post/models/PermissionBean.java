package com.huanchengfly.tieba.post.models;

import androidx.annotation.DrawableRes;

public class PermissionBean {
    private int id;
    private String data;
    private String title;
    private int icon;

    public PermissionBean(int id, String data, String title, @DrawableRes int icon) {
        this.id = id;
        this.data = data;
        this.title = title;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public PermissionBean setId(int id) {
        this.id = id;
        return this;
    }

    public String getData() {
        return data;
    }

    public PermissionBean setData(String data) {
        this.data = data;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public PermissionBean setTitle(String title) {
        this.title = title;
        return this;
    }

    public int getIcon() {
        return icon;
    }

    public PermissionBean setIcon(@DrawableRes int icon) {
        this.icon = icon;
        return this;
    }
}
