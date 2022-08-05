package com.huanchengfly.tieba.post.interfaces

import android.webkit.WebView

interface OnOverrideUrlLoadingListener {
    fun shouldOverrideUrlLoading(view: WebView, urlString: String?): Boolean
}