package com.huanchengfly.tieba.post.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.huanchengfly.tieba.post.utils.TiebaUtil

class AutoSignAlarm : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        TiebaUtil.startSign(context)
    }

    companion object {
        val TAG = AutoSignAlarm::class.java.simpleName
    }
}