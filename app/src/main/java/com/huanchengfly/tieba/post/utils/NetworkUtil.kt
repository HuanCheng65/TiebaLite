package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.huanchengfly.tieba.post.App

fun isNetworkConnected(context: Context = App.INSTANCE): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        val activeNetwork = connectivityManager.activeNetworkInfo ?: return false
        return activeNetwork.isConnected
    } else {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}

object NetworkUtil {
    fun isNetworkConnected(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        val mConnectivityManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mNetworkInfo = mConnectivityManager.activeNetworkInfo ?: return false
        return mNetworkInfo.isConnected
    }

    fun isWifiConnected(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        val mConnectivityManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mWiFiNetworkInfo = mConnectivityManager
            .getNetworkInfo(ConnectivityManager.TYPE_WIFI) ?: return false
        return mWiFiNetworkInfo.isConnected
    }

    fun isMobileConnected(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        val mConnectivityManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mMobileNetworkInfo = mConnectivityManager
            .getNetworkInfo(ConnectivityManager.TYPE_MOBILE) ?: return false
        return mMobileNetworkInfo.isConnected
    }
}