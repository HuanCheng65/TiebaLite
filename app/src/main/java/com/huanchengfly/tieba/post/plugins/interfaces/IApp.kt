package com.huanchengfly.tieba.post.plugins.interfaces

import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AlertDialog

interface IApp {
    fun getAppContext(): Context

    fun getCurrentContext(): Context

    fun launchUrl(url: String)

    fun showLoadingDialog(): Dialog

    fun toastShort(text: String)

    fun copyText(text: String)

    fun showAlertDialog(builder: AlertDialog.Builder.() -> Unit): AlertDialog
}