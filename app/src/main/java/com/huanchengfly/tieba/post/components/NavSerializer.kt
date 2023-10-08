package com.huanchengfly.tieba.post.components

import android.util.Log
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.urlDecode
import com.huanchengfly.tieba.post.api.urlEncode
import com.ramcosta.composedestinations.navargs.DestinationsNavTypeSerializer
import com.ramcosta.composedestinations.navargs.NavTypeSerializer
import com.ramcosta.composedestinations.navargs.utils.base64ToByteArray
import com.ramcosta.composedestinations.navargs.utils.toBase64Str

@NavTypeSerializer
class ThreadInfoSerializer : DestinationsNavTypeSerializer<ThreadInfo> {
    override fun toRouteString(value: ThreadInfo): String {
        val routeStr = ThreadInfo.ADAPTER.encode(value).toBase64Str().urlEncode()
        Log.d("ThreadInfoSerializer", "toRouteString: $routeStr")
        return routeStr
    }

    override fun fromRouteString(routeStr: String): ThreadInfo {
        Log.d("ThreadInfoSerializer", "fromRouteString: $routeStr")
        return ThreadInfo.ADAPTER.decode(routeStr.urlDecode().base64ToByteArray())
    }
}