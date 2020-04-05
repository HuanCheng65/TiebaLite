package com.huanchengfly.tieba.api.models;

import com.huanchengfly.utils.GsonUtil;

public class CollectDataBean {
    public String pid;
    public String tid;
    public String status;
    public String type;

    public CollectDataBean(String pid, String tid, String status, String type) {
        this.pid = pid;
        this.tid = tid;
        this.status = status;
        this.type = type;
    }

    public CollectDataBean setPid(String pid) {
        this.pid = pid;
        return this;
    }

    public CollectDataBean setStatus(String status) {
        this.status = status;
        return this;
    }

    public CollectDataBean setTid(String tid) {
        this.tid = tid;
        return this;
    }

    public CollectDataBean setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        return GsonUtil.getGson().toJson(this);
    }
}
