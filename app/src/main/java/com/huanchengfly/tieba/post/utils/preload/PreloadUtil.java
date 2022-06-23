package com.huanchengfly.tieba.post.utils.preload;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.billy.android.preloader.PreLoader;
import com.billy.android.preloader.interfaces.DataLoader;

public final class PreloadUtil {
    public static final String EXTRA_PRELOAD_ID = "preload_id";

    public static void startActivityWithPreload(Context context, Intent intent, DataLoader dataLoader) {
        context.startActivity(intent.putExtra(EXTRA_PRELOAD_ID, PreLoader.preLoad(dataLoader)));
    }

    public static boolean isPreloading(Activity context) {
        int id = getPreloadId(context);
        return id != -1 && PreLoader.exists(id);
    }

    public static int getPreloadId(Activity context) {
        return context.getIntent().getIntExtra(EXTRA_PRELOAD_ID, -1);
    }
}
