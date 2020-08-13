package com.huanchengfly.tieba.api

import android.annotation.SuppressLint
import android.content.Context
import com.huanchengfly.tieba.api.interfaces.CommonAPICallback
import com.huanchengfly.tieba.api.models.ChangelogBean
import com.huanchengfly.tieba.api.models.NewUpdateBean
import com.huanchengfly.tieba.api.models.UpdateInfoBean
import com.huanchengfly.tieba.post.base.BaseApplication
import com.huanchengfly.tieba.post.utils.SharedPreferencesUtil
import com.huanchengfly.tieba.post.utils.VersionUtil
import com.tsy.sdk.myokhttp.MyOkHttp
import com.tsy.sdk.myokhttp.response.GsonResponseHandler
import io.michaelrocks.paranoid.Obfuscate
import java.lang.ref.WeakReference

@Obfuscate
class LiteApi private constructor(context: Context) {
    private val myOkHttp: MyOkHttp = MyOkHttp()
    private val contextWeakReference: WeakReference<Context> = WeakReference(context)
    val context: Context?
        get() = contextWeakReference.get()

    fun changelog(apiCallback: CommonAPICallback<ChangelogBean?>) {
        val builder = myOkHttp.get()
                .url(Url.CHANGELOG + VersionUtil.getVersionCode(context))
        val oldVersion = SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_APP_DATA).getInt("version", -1)
        if (oldVersion != -1) {
            builder.addParam("update_from", oldVersion.toString())
        }
        builder.enqueue(object : GsonResponseHandler<ChangelogBean>() {
            override fun onFailure(statusCode: Int, error_msg: String) {
                apiCallback.onFailure(statusCode, error_msg)
            }

            override fun onSuccess(statusCode: Int, response: ChangelogBean) {
                if (response.isSuccess) {
                    apiCallback.onSuccess(response)
                } else {
                    apiCallback.onFailure(response.errorCode, response.errorMsg)
                }
            }
        })
    }

    fun newCheckUpdate(apiCallback: CommonAPICallback<NewUpdateBean?>) {
        val beta = SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_SETTINGS).getBoolean("check_beta_update", false)
        myOkHttp.get()
                .url(Url.CHECK_UPDATE)
                .addParam("version_code", VersionUtil.getVersionCode(context).toString())
                .addParam("beta", beta.toString())
                .addParam("lang", getLanguage())
                .enqueue(object : GsonResponseHandler<NewUpdateBean>() {
                    override fun onFailure(statusCode: Int, error_msg: String) {
                        apiCallback.onFailure(statusCode, error_msg)
                    }

                    override fun onSuccess(statusCode: Int, response: NewUpdateBean) {
                        if (response.isSuccess == true) {
                            apiCallback.onSuccess(response)
                        } else {
                            response.errorCode?.let { apiCallback.onFailure(it, response.errorMsg) }
                        }
                    }
                })
    }

    fun updateInfo(apiCallback: CommonAPICallback<UpdateInfoBean?>) {
        myOkHttp.get()
                .url(Url.UPDATE_INFO)
                .enqueue(object : GsonResponseHandler<UpdateInfoBean?>() {
                    override fun onFailure(statusCode: Int, error_msg: String) {
                        apiCallback.onFailure(statusCode, error_msg)
                    }

                    override fun onSuccess(statusCode: Int, response: UpdateInfoBean?) {
                        apiCallback.onSuccess(response)
                    }
                })
    }

    companion object {
        const val TAG = "LiteApi"
        @JvmStatic
        @get:Synchronized
        @SuppressLint("StaticFieldLeak")
        var instance: LiteApi? = null
            get() {
                if (field == null) {
                    synchronized(LiteApi::class.java) {
                        if (field == null) {
                            field = LiteApi(BaseApplication.getInstance())
                        }
                    }
                }
                return field
            }
            private set

    }

}