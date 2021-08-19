package com.huanchengfly.tieba.post.api.retrofit

import android.os.Build
import android.webkit.WebSettings
import com.huanchengfly.tieba.post.BaseApplication
import com.huanchengfly.tieba.post.api.Header
import com.huanchengfly.tieba.post.api.Param
import com.huanchengfly.tieba.post.api.interceptors.SortAndSignInterceptor
import com.huanchengfly.tieba.post.api.retrofit.adapter.DeferredCallAdapterFactory
import com.huanchengfly.tieba.post.api.retrofit.converter.gson.GsonConverterFactory
import com.huanchengfly.tieba.post.api.retrofit.interceptors.*
import com.huanchengfly.tieba.post.api.retrofit.interfaces.MiniTiebaApi
import com.huanchengfly.tieba.post.api.retrofit.interfaces.NewTiebaApi
import com.huanchengfly.tieba.post.api.retrofit.interfaces.OfficialTiebaApi
import com.huanchengfly.tieba.post.api.retrofit.interfaces.WebTiebaApi
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.MobileInfoUtil
import com.huanchengfly.tieba.post.utils.UIDUtil
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit


object RetrofitTiebaApi {
    private val initTime = System.currentTimeMillis()
    private val clientId = "wappc_${initTime}_${Math.round(Math.random() * 1000).toInt()}"
    private val stParamInterceptor = StParamInterceptor()
    private val connectionPool = ConnectionPool()

    private val defaultCommonParamInterceptor = CommonParamInterceptor(
        Param.BDUSS to { AccountUtil.getBduss(BaseApplication.instance) },
        Param.CLIENT_ID to { clientId },
        Param.CLIENT_TYPE to { "2" },
        Param.OS_VERSION to { Build.VERSION.SDK_INT.toString() },
        Param.MODEL to { Build.MODEL },
        Param.NET_TYPE to { "1" },
        Param.PHONE_IMEI to { MobileInfoUtil.getIMEI(BaseApplication.instance) },
        Param.TIMESTAMP to { System.currentTimeMillis().toString() }
    )

    private val defaultCommonHeaderInterceptor =
        CommonHeaderInterceptor(
            Header.COOKIE to { "ka=open" },
            Header.PRAGMA to { "no-cache" }
        )
    private val gsonConverterFactory = GsonConverterFactory.create()
    private val sortAndSignInterceptor = SortAndSignInterceptor("tiebaclient!!!")

    val NEW_TIEBA_API: NewTiebaApi by lazy {
        createAPI<NewTiebaApi>("http://c.tieba.baidu.com/",
            defaultCommonHeaderInterceptor,
            CommonHeaderInterceptor(
                Header.USER_AGENT to { "bdtb for Android 8.2.2" },
                Header.CUID to { UIDUtil.getFinalCUID() }
            ),
            defaultCommonParamInterceptor,
            stParamInterceptor,
            CommonParamInterceptor(
                Param.CUID to { UIDUtil.getFinalCUID() },
                Param.FROM to { "baidu_appstore" },
                Param.CLIENT_VERSION to { "8.2.2" }
            ))
    }

    val WEB_TIEBA_API: WebTiebaApi by lazy {
        createAPI<WebTiebaApi>("https://tieba.baidu.com/",
            CommonHeaderInterceptor(
                Header.HOST to { "tieba.baidu.com" },
                Header.USER_AGENT to { WebSettings.getDefaultUserAgent(BaseApplication.instance) }
            ),
            AddCookieInterceptor)
    }

    val MINI_TIEBA_API: MiniTiebaApi by lazy {
        createAPI<MiniTiebaApi>("http://c.tieba.baidu.com/",
            defaultCommonHeaderInterceptor,
            CommonHeaderInterceptor(
                Header.USER_AGENT to { "bdtb for Android 7.2.0.0" },
                Header.CUID to { UIDUtil.getFinalCUID() },
                Header.CUID_GALAXY2 to { UIDUtil.getFinalCUID() }
            ),
            defaultCommonParamInterceptor,
            stParamInterceptor,
            CommonParamInterceptor(
                Param.CUID to { UIDUtil.getFinalCUID() },
                Param.CUID_GALAXY2 to { UIDUtil.getFinalCUID() },
                Param.FROM to { "1021636m" },
                Param.CLIENT_VERSION to { "7.2.0.0" },
                Param.SUBAPP_TYPE to { "mini" }
            ))
    }

    val OFFICIAL_TIEBA_API: OfficialTiebaApi by lazy {
        createAPI<OfficialTiebaApi>("http://c.tieba.baidu.com/",
            defaultCommonHeaderInterceptor,
            CommonHeaderInterceptor(
                Header.USER_AGENT to { "bdtb for Android 9.9.8.32" },
                Header.CUID to { UIDUtil.getNewCUID() },
                Header.CUID_GALAXY2 to { UIDUtil.getFinalCUID() },
                Header.CUID_GID to { "" }
            ),
            defaultCommonParamInterceptor,
            stParamInterceptor,
            CommonParamInterceptor(
                Param.CUID to { UIDUtil.getNewCUID() },
                Param.CUID_GALAXY2 to { UIDUtil.getFinalCUID() },
                Param.CUID_GID to { "" },
                Param.FROM to { "tieba" },
                Param.CLIENT_VERSION to { "9.9.8.32" }
            ))
    }

    private inline fun <reified T : Any> createAPI(
        baseUrl: String,
        vararg interceptors: Interceptor
    ) = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addCallAdapterFactory(DeferredCallAdapterFactory.invoke())
        .addConverterFactory(NullOnEmptyConverterFactory())
        .addConverterFactory(gsonConverterFactory)
        .client(OkHttpClient.Builder().apply {
            interceptors.forEach {
                addInterceptor(it)
            }
            addInterceptor(sortAndSignInterceptor)
            addInterceptor(FailureResponseInterceptor)
            addInterceptor(ForceLoginInterceptor)
            connectionPool(connectionPool)
        }.build())
        .build()
        .create(T::class.java)
}