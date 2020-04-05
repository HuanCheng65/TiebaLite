package com.huanchengfly.tieba.post.utils;

import android.content.Context;

import com.huanchengfly.tieba.post.models.database.History;

import org.litepal.LitePal;

import java.util.List;

public class HistoryHelper {
    public static final int TYPE_URL = 0;
    public static final int TYPE_BA = 1;
    public static final int TYPE_THREAD = 2;

    private Context mContext;

    public HistoryHelper(Context context) {
        this.mContext = context;
    }

    public void delete() {
        LitePal.deleteAll(History.class);
    }

    public void writeHistory(String data, String title, int type) {
        add(data, "", title, type);
    }

    public void writeHistory(String data, String extras, String title, int type) {
        add(data, extras, title, type);
    }

    public void writeHistory(History history) {
        add(history);
    }

    public List<History> getAll() {
        return LitePal.order("timestamp desc,count desc").limit(100).find(History.class);
    }

    private void add(String data, String extras, String title, int type) {
        History historyBean = LitePal.where("data = ?", data).findFirst(History.class);
        if (historyBean != null) {
            historyBean.setTimestamp(System.currentTimeMillis())
                    .setTitle(title)
                    .setExtras(extras)
                    .setCount(historyBean.getCount() + 1)
                    .update(historyBean.getId());
            return;
        }
        add(new History()
                .setData(data)
                .setExtras(extras)
                .setTitle(title)
                .setType(type));
    }

    private boolean update(History history) {
        History historyBean = LitePal.where("data = ?", history.getData()).findFirst(History.class);
        if (historyBean != null) {
            historyBean.setTimestamp(System.currentTimeMillis())
                    .setTitle(history.getTitle())
                    .setExtras(history.getExtras())
                    .setAvatar(history.getAvatar())
                    .setUsername(history.getUsername())
                    .setCount(historyBean.getCount() + 1)
                    .update(historyBean.getId());
            return true;
        }
        return false;
    }

    private void add(History history) {
        if (update(history)) {
            return;
        }
        history.setCount(1)
                .setTimestamp(System.currentTimeMillis())
                .save();
    }
}