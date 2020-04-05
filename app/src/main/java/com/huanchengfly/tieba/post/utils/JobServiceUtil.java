package com.huanchengfly.tieba.post.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class JobServiceUtil {
    public static int getJobId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("appData", Context.MODE_PRIVATE);
        int jobId = sharedPreferences.getInt("jobId", -1);
        if (jobId == -1) {
            jobId = (int) (Math.random() * (99999 + 1));
            sharedPreferences.edit()
                    .putInt("jobId", jobId)
                    .apply();
        }
        return jobId;
    }
}
