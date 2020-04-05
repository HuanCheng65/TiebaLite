package com.huanchengfly.tieba.post.models.database;

import org.litepal.crud.LitePalSupport;

public class Account extends LitePalSupport {
    private int id;
    private String uid;
    private String name;
    private String nameShow;
    private String bduss;
    private String tbs;
    private String itbTbs;
    private String portrait;
    private String sToken;
    private String cookie;

    public String getCookie() {
        return cookie;
    }

    public Account setCookie(String cookie) {
        this.cookie = cookie;
        return this;
    }

    public String getsToken() {
        return sToken;
    }

    public Account setsToken(String sToken) {
        this.sToken = sToken;
        return this;
    }

    public String getItbTbs() {
        return itbTbs;
    }

    public Account setItbTbs(String itbTbs) {
        this.itbTbs = itbTbs;
        return this;
    }

    public int getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }

    public Account setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public String getName() {
        return name;
    }

    public Account setName(String name) {
        this.name = name;
        return this;
    }

    public String getNameShow() {
        return nameShow;
    }

    public Account setNameShow(String nameShow) {
        this.nameShow = nameShow;
        return this;
    }

    public String getBduss() {
        return bduss;
    }

    public Account setBduss(String bduss) {
        this.bduss = bduss;
        return this;
    }

    public String getTbs() {
        return tbs;
    }

    public Account setTbs(String tbs) {
        this.tbs = tbs;
        return this;
    }

    public String getPortrait() {
        return portrait;
    }

    public Account setPortrait(String portrait) {
        this.portrait = portrait;
        return this;
    }
}