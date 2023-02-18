package com.huanchengfly.tieba.post.components

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.github.gzuliyujiang.oaid.DeviceID
import com.github.gzuliyujiang.oaid.IGetter
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.utils.helios.Base32

object OAIDGetter : Application.ActivityLifecycleCallbacks, IGetter {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {
        if (!App.Config.inited) {
            App.Config.isOAIDSupported = DeviceID.supportedOAID(activity)
            if (App.Config.isOAIDSupported) {
                DeviceID.getOAID(activity, this)
            } else {
                App.Config.inited = true
                App.Config.statusCode = -200
                App.Config.isTrackLimited = false
            }
        }
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

    override fun onOAIDGetComplete(result: String) {
        App.Config.inited = true
        App.Config.oaid = result
        App.Config.encodedOAID = Base32.encode(result.encodeToByteArray())
        App.Config.statusCode = 0
        App.Config.isTrackLimited = false
    }

    override fun onOAIDGetError(error: Exception?) {
        App.Config.inited = true
        App.Config.statusCode = -100
        App.Config.isTrackLimited = true
    }
}