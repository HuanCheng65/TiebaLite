package com.huanchengfly.tieba.post.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.huanchengfly.tieba.post.utils.TiebaUtil;

public class AutoSignAlarm extends BroadcastReceiver {
    public static final String TAG = AutoSignAlarm.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        TiebaUtil.startSign(context);
    }
}