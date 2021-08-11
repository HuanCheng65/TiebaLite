package com.huanchengfly.tieba.post.plugins.interfaces

import android.content.Context

interface IApp {
    fun getAppContext(): Context

    fun launchUrl(url: String)
}