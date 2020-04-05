package com.huanchengfly.tieba.post.models;

public class SignDataBean {
    private String kw;
    private String tbs;

    public SignDataBean(String kw, String tbs) {
        this.kw = kw;
        this.tbs = tbs;
    }

    public String getKw() {
        return kw;
    }

    public SignDataBean setKw(String kw) {
        this.kw = kw;
        return this;
    }

    public String getTbs() {
        return tbs;
    }

    public SignDataBean setTbs(String tbs) {
        this.tbs = tbs;
        return this;
    }
}
