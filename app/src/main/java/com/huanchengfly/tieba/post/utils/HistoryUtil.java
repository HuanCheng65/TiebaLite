package com.huanchengfly.tieba.post.utils;

import com.huanchengfly.tieba.post.models.database.History;

import org.litepal.LitePal;
import org.litepal.crud.async.FindMultiExecutor;

import java.util.List;

public final class HistoryUtil {
    public static final int TYPE_FORUM = 1;
    public static final int TYPE_THREAD = 2;

    private HistoryUtil() {
    }

    public static void deleteAll() {
        LitePal.deleteAll(History.class);
    }

    public static void writeHistory(History history) {
        writeHistory(history, false);
    }

    public static void writeHistory(History history, boolean async) {
        add(history, async);
    }

    public static List<History> getAll() {
        return LitePal.order("timestamp desc, count desc").limit(100).find(History.class);
    }

    public static List<History> getAll(int type) {
        return LitePal.order("timestamp desc, count desc").where("type = ?", String.valueOf(type)).limit(100).find(History.class);
    }

    public static FindMultiExecutor<History> getAllAsync(int type) {
        return LitePal.order("timestamp desc, count desc").where("type = ?", String.valueOf(type)).limit(100).findAsync(History.class);
    }

    private static boolean update(History history) {
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

    private static void add(History history, boolean async) {
        if (update(history)) {
            return;
        }
        history.setCount(1)
                .setTimestamp(System.currentTimeMillis());
        if (async) {
            history.saveAsync().listen(null);
        } else {
            history.save();
        }
    }

    private static void add(History history) {
        add(history, false);
    }
}