package com.huanchengfly.tieba.post.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.api.models.NewUpdateBean;

import java.util.ArrayList;
import java.util.List;

public class VersionUtil {
    public static int getVersionCode(Context mContext) {
        int versionCode = 0;
        try {
            versionCode = mContext.getPackageManager().
                    getPackageInfo(mContext.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    public static String getPackageName(Context context) {
        String packageName = "";
        try {
            packageName = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).packageName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageName;
    }

    public static String getVersionName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }

    public static void showDownloadDialog(Context context, NewUpdateBean.ResultBean versionInfo) {
        List<String> list = new ArrayList<>();
        for (NewUpdateBean.DownloadBean downloadBean : versionInfo.getDownloads()) {
            list.add(downloadBean.getName());
        }
        String[] arr = list.toArray(new String[0]);
        DialogUtil.build(context)
                .setTitle(R.string.title_dialog_download)
                .setCancelable(versionInfo.isCancelable())
                .setItems(arr, (dialog, which) -> {
                    NewUpdateBean.DownloadBean downloadBean = versionInfo.getDownloads().get(which);
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(downloadBean.getUrl())));
                })
                .create()
                .show();
    }
}
