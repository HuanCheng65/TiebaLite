package com.huanchengfly.tieba.post.utils;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

public class TiebaLiteJavaScript {
    public static final String TAG = "JsBridge";

    private static Handler handler = new Handler();
    public Context context;
    public WebView webView;

    public TiebaLiteJavaScript(WebView webView) {
        this.context = webView.getContext();
        this.webView = webView;
    }

    @JavascriptInterface
    public void toast(final String text) {
        handler.post(() -> {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        });
    }

    @JavascriptInterface
    public String getTimeFromNow(String time) {
        return DateTimeUtils.getRelativeTimeString(context, time);
    }

    @JavascriptInterface
    public String getTheme() {
        return ThemeUtil.getTheme(context);
    }

    @JavascriptInterface
    public void copyText(String content) {
        TiebaUtil.copyText(context, content);
    }

    @JavascriptInterface
    public void putString(String key, String value) {
        SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_WEBVIEW_INFO)
                .edit()
                .putString(key, value)
                .apply();
        Log.i(TAG, "putString: " + key + ": " + value);
    }

    @JavascriptInterface
    public String getString(String key) {
        return SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_WEBVIEW_INFO)
                .getString(key, "");
    }

    @JavascriptInterface
    public int getInt(String key, int defValue) {
        return SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_WEBVIEW_INFO)
                .getInt(key, defValue);
    }

    @JavascriptInterface
    public void putInt(String key, int value) {
        SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_WEBVIEW_INFO)
                .edit()
                .putInt(key, value)
                .apply();
        Log.i(TAG, "putInt: " + key + ": " + value);
    }
}