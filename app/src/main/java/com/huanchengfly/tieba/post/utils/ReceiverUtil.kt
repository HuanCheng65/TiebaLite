package com.huanchengfly.tieba.post.utils;

import android.content.IntentFilter;

public class ReceiverUtil {
    public static IntentFilter createIntentFilter(String action) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(action);
        return filter;
    }
}
