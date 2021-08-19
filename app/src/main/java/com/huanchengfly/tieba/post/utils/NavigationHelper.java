package com.huanchengfly.tieba.post.utils;

import static com.huanchengfly.tieba.post.activities.ForumActivity.EXTRA_FORUM_NAME;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.activities.FloorActivity;
import com.huanchengfly.tieba.post.activities.ForumActivity;
import com.huanchengfly.tieba.post.activities.LoginActivity;
import com.huanchengfly.tieba.post.activities.ThreadActivity;
import com.huanchengfly.tieba.post.activities.UserActivity;
import com.huanchengfly.tieba.post.activities.WebViewActivity;
import com.huanchengfly.tieba.post.components.dialogs.PermissionDialog;
import com.huanchengfly.tieba.post.models.PermissionBean;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public final class NavigationHelper {
    public static final String TAG = "NavigationHelper";
    public static final int ACTION_LOGIN = 1;
    public static final int ACTION_FORUM = 2;
    public static final int ACTION_THREAD = 3;
    public static final int ACTION_URL = 4;
    public static final int ACTION_FLOOR = 5;
    public static final int ACTION_THREAD_POST = 6;
    public static final int ACTION_USER = 7;
    public static final int ACTION_USER_BY_UID = 8;
    private Context mContext;
    private Activity activity;
    private String activityName;
    private boolean isActivityContext;

    private NavigationHelper(Context context) {
        this(context, context.getClass().getSimpleName());
    }

    private NavigationHelper(Context context, String activityName) {
        this.mContext = context;
        this.activityName = activityName;
        if (context instanceof Activity) {
            this.isActivityContext = true;
            this.activity = (Activity) context;
        }
    }

    public static NavigationHelper newInstance(Context context) {
        return new NavigationHelper(context);
    }

    public static NavigationHelper newInstance(Context context, String activityName) {
        return new NavigationHelper(context, activityName);
    }

    public static void toUserSpaceWithAnim(Context context, String uid, String avatarUrl, View avatarView) {
        if (context instanceof Activity) {
            avatarView.setTransitionName("avatar");
            context.startActivity(new Intent(context, UserActivity.class)
                    .putExtra(UserActivity.EXTRA_UID, uid)
                    .putExtra(UserActivity.EXTRA_AVATAR, avatarUrl), ActivityOptions.makeSceneTransitionAnimation((Activity) context, avatarView, "avatar").toBundle());
        } else if (context != null) {
            context.startActivity(new Intent(context, UserActivity.class)
                    .putExtra(UserActivity.EXTRA_UID, uid)
                    .putExtra(UserActivity.EXTRA_AVATAR, avatarUrl));
        }
    }

    public boolean interceptWebViewRequest(WebView mWebView, WebResourceRequest request) {
        return navigationByUrl(request.getUrl().toString(), mWebView.getUrl());
    }

    public boolean interceptWebViewRequest(WebView mWebView, String url) {
        return navigationByUrl(url, mWebView.getUrl());
    }

    public void navigationByData(int action) {
        switch (action) {
            case ACTION_LOGIN:
                startActivity(new Intent(mContext, LoginActivity.class));
                break;
        }
    }

    private void startActivity(Intent intent) {
        try {
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(mContext, R.string.toast_nav_failed, Toast.LENGTH_SHORT).show();
        }
    }

    public void navigationByData(int action, String data) {
        switch (action) {
            case ACTION_FORUM:
                Intent intent = new Intent(mContext, ForumActivity.class);
                intent.putExtra(EXTRA_FORUM_NAME, data);
                startActivity(intent);
                break;
            case ACTION_URL:
                navigationByUrl(data);
                break;
            case ACTION_THREAD_POST:
                navigationByUrl("https://tieba.baidu.com/mo/q/thread_post?word=" + data);
                break;
            case ACTION_USER:
                navigationByUrl(mContext.getString(R.string.url_user_home, data, 0));
                break;
            case ACTION_USER_BY_UID:
                startActivity(new Intent(mContext, UserActivity.class).putExtra(UserActivity.EXTRA_UID, data));
                break;
        }
    }

    public void navigationByData(int action, Map<String, String> data) {
        switch (action) {
            case ACTION_THREAD:
                String tid = data.get("tid");
                String pid = data.get("pid");
                String seeLzStr = data.get("seeLz");
                String from = data.get("from");
                String maxPid = data.get("max_pid");
                if (tid != null) {
                    pid = pid == null ? "" : pid;
                    from = from == null ? "" : from;
                    maxPid = maxPid == null ? "" : maxPid;
                    boolean seeLz = (seeLzStr != null && seeLzStr.equalsIgnoreCase("1"));
                    startActivity(new Intent(mContext, ThreadActivity.class)
                            .putExtra("tid", tid)
                            .putExtra("pid", pid)
                            .putExtra("from", from)
                            .putExtra("max_pid", maxPid)
                            .putExtra("seeLz", seeLz));
                }
                break;
            case ACTION_FLOOR:
                String floorTid = data.get("tid");
                String floorPid = data.get("pid");
                String floorSPid = data.get("spid");
                if (floorTid != null) {
                    pid = floorPid == null ? "" : floorPid;
                    String spid = floorSPid == null ? "" : floorSPid;
                    startActivity(new Intent(mContext, FloorActivity.class)
                            .putExtra("tid", floorTid)
                            .putExtra("pid", pid)
                            .putExtra("spid", spid));
                }
                break;
        }
    }

    private boolean navigationByUrl(String url) {
        return navigationByUrl(url, "");
    }

    private boolean navigationByUrl(String url, @NonNull String oldUrl) {
        if (url == null || oldUrl == null) {
            return false;
        }
        Uri uri = Uri.parse(url);
        Uri oldUri = Uri.parse(oldUrl);
        String host = uri.getHost(),
                path = uri.getPath(),
                oldHost = oldUri.getHost(),
                oldPath = oldUri.getPath(),
                scheme = uri.getScheme();
        if (host == null || scheme == null || path == null) {
            return false;
        }
        if (path.equalsIgnoreCase("/mo/q/checkurl")) {
            url = uri.getQueryParameter("url");
            uri = Uri.parse(url);
            host = uri.getHost();
            path = uri.getPath();
            scheme = uri.getScheme();
            if (host == null || scheme == null || path == null) {
                return false;
            }
        }
        if (scheme.startsWith("http") || scheme.startsWith("file")) {
            if (host.equalsIgnoreCase("wapp.baidu.com") || host.equalsIgnoreCase("tieba.baidu.com") || host.equalsIgnoreCase("tiebac.baidu.com")) {
                if (path.equalsIgnoreCase("/f") || path.equalsIgnoreCase("/mo/q/m")) {
                    String kw = uri.getQueryParameter("kw");
                    String word = uri.getQueryParameter("word");
                    String kz = uri.getQueryParameter("kz");
                    if (kw != null) {
                        if (activityName.startsWith("WebViewActivity") && isPostUri(oldUri)) {
                            if (this.isActivityContext && activity != null) activity.finish();
                            return true;
                        } else if (!activityName.startsWith("ForumActivity")) {
                            navigationByData(ACTION_FORUM, kw);
                            return true;
                        }
                    } else if (word != null) {
                        if (!activityName.startsWith("ForumActivity")) {
                            navigationByData(ACTION_FORUM, word);
                            return true;
                        }
                    } else if (kz != null) {
                        Intent intent = new Intent(mContext, ThreadActivity.class);
                        intent.putExtra("url", url);
                        startActivity(intent);
                        return true;
                    }
                } else if (path.startsWith("/index/tbwise") || path.equalsIgnoreCase("/")) {
                    if (activityName.startsWith("ForumActivity")) {
                        if (this.isActivityContext) activity.finish();
                        Toast.makeText(mContext, "没找到内容鸭", Toast.LENGTH_SHORT).show();
                    } else if (oldPath != null && oldPath.startsWith("/mo/q/accountstatus")) {
                        if (this.isActivityContext) activity.finish();
                    }
                    return false;
                } else if (path.startsWith("/p/")) {
                    Intent intent = new Intent(mContext, ThreadActivity.class);
                    intent.putExtra("url", url);
                    startActivity(intent);
                    return true;
                }
            }
            if (!path.contains("android_asset")) {
                if (!(activityName.startsWith("WebViewActivity") || activityName.startsWith("LoginActivity"))) {
                    boolean isTiebaLink = host.contains("tieba.baidu.com") || host.contains("wappass.baidu.com") || host.contains("ufosdk.baidu.com") || host.contains("m.help.baidu.com");
                    if (isTiebaLink || SharedPreferencesUtil.get(mContext, SharedPreferencesUtil.SP_SETTINGS).getBoolean("use_webview", true)) {
                        startActivity(new Intent(mContext, WebViewActivity.class).putExtra("url", url));
                        return true;
                    } else {
                        if (SharedPreferencesUtil.get(mContext, SharedPreferencesUtil.SP_SETTINGS).getBoolean("use_custom_tabs", true)) {
                            CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder()
                                    .setShowTitle(true)
                                    .setToolbarColor(ThemeUtils.getColorByAttr(mContext, R.attr.colorToolbar));
                            try {
                                intentBuilder.build().launchUrl(mContext, uri);
                            } catch (ActivityNotFoundException e) {
                                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                            }
                        } else {
                            startActivity(new Intent(Intent.ACTION_VIEW, uri));
                        }
                    }
                }
            }
            return false;
        } else if (scheme.startsWith("intent")) {
            Intent intent;
            String appName;
            try {
                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setComponent(null);
                intent.setSelector(null);
                List<ResolveInfo> resolves = mContext.getPackageManager().queryIntentActivities(intent, 0);
                if (resolves.size() > 0) {
                    if (resolves.size() == 1) {
                        ResolveInfo resolveInfo = resolves.get(0);
                        PackageManager pManager = mContext.getPackageManager();
                        appName = resolveInfo.loadLabel(pManager).toString();
                    } else {
                        appName = mContext.getString(R.string.name_multiapp);
                    }
                    new PermissionDialog(mContext,
                            new PermissionBean(PermissionDialog.CustomPermission.PERMISSION_START_APP,
                                    oldHost + scheme,
                                    mContext.getString(R.string.title_start_app_permission, oldHost, appName),
                                    R.drawable.ic_round_exit_to_app))
                            .setOnGrantedCallback(isForever -> startActivity(intent))
                            .show();
                }
                return true;
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            try {
                String appName;
                final Intent intent = new Intent(Intent.ACTION_VIEW,
                        uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                List<ResolveInfo> resolves = mContext.getPackageManager().queryIntentActivities(intent, 0);
                if (resolves.size() > 0) {
                    if (resolves.size() == 1) {
                        ResolveInfo resolveInfo = resolves.get(0);
                        PackageManager pManager = mContext.getPackageManager();
                        appName = resolveInfo.loadLabel(pManager).toString();
                    } else {
                        appName = mContext.getString(R.string.name_multiapp);
                    }
                    new PermissionDialog(mContext,
                            new PermissionBean(PermissionDialog.CustomPermission.PERMISSION_START_APP,
                                    oldHost + scheme,
                                    mContext.getString(R.string.title_start_app_permission, oldHost, appName),
                                    R.drawable.ic_round_exit_to_app))
                            .setOnGrantedCallback(isForever -> startActivity(intent))
                            .show();
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private boolean isPostUri(Uri uri) {
        String host = uri.getHost();
        String path = uri.getPath();
        String word = uri.getQueryParameter("word");
        if (host == null || path == null) {
            return false;
        }
        return path.equalsIgnoreCase("/mo/q/thread_post") && word != null;
    }
}