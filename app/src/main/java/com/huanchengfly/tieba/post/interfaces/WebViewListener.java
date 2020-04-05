package com.huanchengfly.tieba.post.interfaces;

import android.graphics.Bitmap;
import android.webkit.WebView;

public interface WebViewListener {
    void onPageFinished(WebView view, String url);

    void onPageStarted(WebView view, String url, Bitmap favicon);
}
