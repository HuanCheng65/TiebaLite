package com.huanchengfly.tieba.post.api.retrofit

import android.os.Build
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.api.ClientVersion
import com.huanchengfly.tieba.post.api.Header
import com.huanchengfly.tieba.post.api.Param
import com.huanchengfly.tieba.post.api.getUserAgent
import com.huanchengfly.tieba.post.api.models.OAID
import com.huanchengfly.tieba.post.api.retrofit.adapter.DeferredCallAdapterFactory
import com.huanchengfly.tieba.post.api.retrofit.adapter.FlowCallAdapterFactory
import com.huanchengfly.tieba.post.api.retrofit.converter.gson.GsonConverterFactory
import com.huanchengfly.tieba.post.api.retrofit.interceptors.AddWebCookieInterceptor
import com.huanchengfly.tieba.post.api.retrofit.interceptors.CommonHeaderInterceptor
import com.huanchengfly.tieba.post.api.retrofit.interceptors.CommonParamInterceptor
import com.huanchengfly.tieba.post.api.retrofit.interceptors.CookieInterceptor
import com.huanchengfly.tieba.post.api.retrofit.interceptors.DropInterceptor
import com.huanchengfly.tieba.post.api.retrofit.interceptors.FailureResponseInterceptor
import com.huanchengfly.tieba.post.api.retrofit.interceptors.ForceLoginInterceptor
import com.huanchengfly.tieba.post.api.retrofit.interceptors.ProtoFailureResponseInterceptor
import com.huanchengfly.tieba.post.api.retrofit.interceptors.SortAndSignInterceptor
import com.huanchengfly.tieba.post.api.retrofit.interceptors.StParamInterceptor
import com.huanchengfly.tieba.post.api.retrofit.interfaces.MiniTiebaApi
import com.huanchengfly.tieba.post.api.retrofit.interfaces.NewTiebaApi
import com.huanchengfly.tieba.post.api.retrofit.interfaces.OfficialProtobufTiebaApi
import com.huanchengfly.tieba.post.api.retrofit.interfaces.OfficialTiebaApi
import com.huanchengfly.tieba.post.api.retrofit.interfaces.WebTiebaApi
import com.huanchengfly.tieba.post.toJson
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.ClientUtils
import com.huanchengfly.tieba.post.utils.CuidUtils
import com.huanchengfly.tieba.post.utils.MobileInfoUtil
import com.huanchengfly.tieba.post.utils.UIDUtil
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.wire.WireConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


object RetrofitTiebaApi {
    private const val READ_TIMEOUT = 60L
    private const val CONNECT_TIMEOUT = 60L
    private const val WRITE_TIMEOUT = 60L

    private val initTime = System.currentTimeMillis()
    internal val randomClientId = "wappc_${initTime}_${(Math.random() * 1000).roundToInt()}"
    private val stParamInterceptor = StParamInterceptor()
    private val connectionPool = ConnectionPool(32, 5, TimeUnit.MINUTES)

    private val defaultCommonParamInterceptor = CommonParamInterceptor(
        Param.BDUSS to { AccountUtil.getBduss() },
        Param.CLIENT_ID to { ClientUtils.clientId },
        Param.CLIENT_TYPE to { "2" },
        Param.OS_VERSION to { Build.VERSION.SDK_INT.toString() },
        Param.MODEL to { Build.MODEL },
        Param.NET_TYPE to { "1" },
        Param.PHONE_IMEI to { MobileInfoUtil.getIMEI(App.INSTANCE) },
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
        createJsonApi<NewTiebaApi>(
            "http://c.tieba.baidu.com/",
            defaultCommonHeaderInterceptor,
            CommonHeaderInterceptor(
                Header.USER_AGENT to { "bdtb for Android 8.2.2" },
                Header.CUID to { UIDUtil.finalCUID }
            ),
            defaultCommonParamInterceptor + CommonParamInterceptor(
                Param.CUID to { UIDUtil.finalCUID },
                Param.FROM to { "baidu_appstore" },
                Param.CLIENT_VERSION to { "8.2.2" }
            ),
            stParamInterceptor,
        )
    }

    val WEB_TIEBA_API: WebTiebaApi by lazy {
        createJsonApi<WebTiebaApi>("https://tieba.baidu.com/",
            CommonHeaderInterceptor(
                Header.USER_AGENT to { getUserAgent("tieba/11.10.8.6 skin/default") },
                Header.CUID to { CuidUtils.getNewCuid() },
                Header.CUID_GALAXY2 to { CuidUtils.getNewCuid() },
                Header.CUID_GID to { "" },
                Header.CUID_GALAXY3 to { UIDUtil.getAid() },
                Header.CLIENT_USER_TOKEN to { AccountUtil.getUid() },
                Header.CHARSET to { "UTF-8" },
                Header.HOST to { "tieba.baidu.com" },
            ),
            AddWebCookieInterceptor
        )
    }

    val MINI_TIEBA_API: MiniTiebaApi by lazy {
        createJsonApi<MiniTiebaApi>(
            "http://c.tieba.baidu.com/",
            defaultCommonHeaderInterceptor,
            CommonHeaderInterceptor(
                Header.USER_AGENT to { "bdtb for Android 7.2.0.0" },
                Header.CUID to { UIDUtil.finalCUID },
                Header.CUID_GALAXY2 to { UIDUtil.finalCUID }
            ),
            defaultCommonParamInterceptor + CommonParamInterceptor(
                Param.CUID to { UIDUtil.finalCUID },
                Param.CUID_GALAXY2 to { UIDUtil.finalCUID },
                Param.FROM to { "1021636m" },
                Param.CLIENT_VERSION to { "7.2.0.0" },
                Param.SUBAPP_TYPE to { "mini" }
            ),
            stParamInterceptor,
        )
    }

    val OFFICIAL_TIEBA_API: OfficialTiebaApi by lazy {
        createJsonApi<OfficialTiebaApi>(
            "http://c.tieba.baidu.com/",
            CommonHeaderInterceptor(
                Header.USER_AGENT to { "bdtb for Android 12.25.1.0" },
                Header.CUID to { CuidUtils.getNewCuid() },
                Header.CUID_GALAXY2 to { CuidUtils.getNewCuid() },
                Header.CUID_GID to { "" },
                Header.CUID_GALAXY3 to { UIDUtil.getAid() },
                Header.CLIENT_TYPE to { "2" },
                Header.CHARSET to { "UTF-8" },
                "client_logid" to { "$initTime" }
            ),
            defaultCommonParamInterceptor + CommonParamInterceptor(
                Param.CUID to { CuidUtils.getNewCuid() },
                Param.CUID_GALAXY2 to { CuidUtils.getNewCuid() },
                Param.CUID_GID to { "" },
                Param.FROM to { "tieba" },
                Param.CLIENT_VERSION to { "12.25.1.0" },
                Param.CUID_GALAXY3 to { UIDUtil.getAid() },
                Param.OAID to { OAID().toJson() },
            ),
            stParamInterceptor,
        )
    }

    val OFFICIAL_PROTOBUF_TIEBA_API: OfficialProtobufTiebaApi by lazy {
        createProtobufApi<OfficialProtobufTiebaApi>(
            "http://c.tieba.baidu.com/",
            CommonHeaderInterceptor(
                Header.CHARSET to { "UTF-8" },
                Header.CLIENT_TYPE to { "2" },
                Header.CLIENT_USER_TOKEN to { AccountUtil.getUid() },
                Header.COOKIE to { "CUID=${CuidUtils.getNewCuid()};ka=open;TBBRAND=${Build.MODEL};" },
                Header.CUID to { CuidUtils.getNewCuid() },
                Header.CUID_GALAXY2 to { CuidUtils.getNewCuid() },
                Header.CUID_GID to { "" },
                Header.CUID_GALAXY3 to { UIDUtil.getAid() },
                Header.USER_AGENT to { "bdtb for Android ${ClientVersion.TIEBA_V11.version}" },
                Header.X_BD_DATA_TYPE to { "protobuf" },
            ),
            defaultCommonParamInterceptor - Param.OS_VERSION + CommonParamInterceptor(
                Param.CUID to { CuidUtils.getNewCuid() },
                Param.CUID_GALAXY2 to { CuidUtils.getNewCuid() },
                Param.CUID_GID to { "" },
                Param.FROM to { "tieba" },
                Param.CLIENT_VERSION to { ClientVersion.TIEBA_V11.version },
                Param.CUID_GALAXY3 to { UIDUtil.getAid() },
                Param.OAID to { OAID().toJson() },
            ),
            stParamInterceptor,
        )
    }

    val OFFICIAL_PROTOBUF_TIEBA_V12_API: OfficialProtobufTiebaApi by lazy {
        createProtobufApi<OfficialProtobufTiebaApi>(
            "http://c.tieba.baidu.com/",
            CommonHeaderInterceptor(
                Header.CHARSET to { "UTF-8" },
                Header.CLIENT_TYPE to { "2" },
                Header.CLIENT_USER_TOKEN to { AccountUtil.getUid() },
                Header.COOKIE to { "CUID=${CuidUtils.getNewCuid()};ka=open;TBBRAND=${Build.MODEL};" },
                Header.CUID to { CuidUtils.getNewCuid() },
                Header.CUID_GALAXY2 to { CuidUtils.getNewCuid() },
                Header.CUID_GID to { "" },
                Header.CUID_GALAXY3 to { UIDUtil.getAid() },
                Header.USER_AGENT to { "bdtb for Android ${ClientVersion.TIEBA_V12.version}" },
                Header.X_BD_DATA_TYPE to { "protobuf" },
            ),
            defaultCommonParamInterceptor - Param.OS_VERSION + CommonParamInterceptor(
                Param.CUID to { CuidUtils.getNewCuid() },
                Param.CUID_GALAXY2 to { CuidUtils.getNewCuid() },
                Param.CUID_GID to { "" },
                Param.FROM to { "tieba" },
                Param.CLIENT_VERSION to { ClientVersion.TIEBA_V12.version },
                Param.CUID_GALAXY3 to { UIDUtil.getAid() },
                Param.OAID to { UIDUtil.getOAID() },
            ),
            stParamInterceptor,
        )
    }

    private inline fun <reified T : Any> createJsonApi(
        baseUrl: String,
        vararg interceptors: Interceptor
    ) = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addCallAdapterFactory(DeferredCallAdapterFactory())
        .addCallAdapterFactory(FlowCallAdapterFactory.create())
        .addConverterFactory(NullOnEmptyConverterFactory())
        .addConverterFactory(gsonConverterFactory)
        .client(OkHttpClient.Builder().apply {
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            interceptors.forEach {
                addInterceptor(it)
            }
            addInterceptor(DropInterceptor)
            addInterceptor(FailureResponseInterceptor)
            addInterceptor(ForceLoginInterceptor)
            addInterceptor(sortAndSignInterceptor)
            connectionPool(connectionPool)
        }.build())
        .build()
        .create(T::class.java)

    private inline fun <reified T : Any> createProtobufApi(
        baseUrl: String,
        vararg interceptors: Interceptor
    ) = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addCallAdapterFactory(DeferredCallAdapterFactory())
        .addCallAdapterFactory(FlowCallAdapterFactory.create())
        .addConverterFactory(NullOnEmptyConverterFactory())
        .addConverterFactory(WireConverterFactory.create())
        .client(OkHttpClient.Builder().apply {
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            interceptors.forEach {
                addInterceptor(it)
            }
            addInterceptor(DropInterceptor)
            addInterceptor(ProtoFailureResponseInterceptor)
            addInterceptor(ForceLoginInterceptor)
            addInterceptor(CookieInterceptor)
            addInterceptor(sortAndSignInterceptor)
            connectionPool(connectionPool)
        }.build())
        .build()
        .create(T::class.java)
}