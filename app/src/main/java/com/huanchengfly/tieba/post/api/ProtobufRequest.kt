package com.huanchengfly.tieba.post.api

import android.content.Context
import android.os.Build
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.api.models.OAID
import com.huanchengfly.tieba.post.api.models.protos.AppPosInfo
import com.huanchengfly.tieba.post.api.models.protos.CommonRequest
import com.huanchengfly.tieba.post.api.models.protos.frsPage.AdParam
import com.huanchengfly.tieba.post.api.retrofit.RetrofitTiebaApi
import com.huanchengfly.tieba.post.api.retrofit.body.MyMultipartBody
import com.huanchengfly.tieba.post.toJson
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.ClientUtils
import com.huanchengfly.tieba.post.utils.CuidUtils
import com.huanchengfly.tieba.post.utils.MobileInfoUtil
import com.huanchengfly.tieba.post.utils.UIDUtil
import com.squareup.wire.Message
import okhttp3.RequestBody.Companion.toRequestBody

const val BOUNDARY = "--------7da3d81520810*"

fun buildProtobufRequestBody(
    data: Message<*, *>,
    clientVersion: ClientVersion = ClientVersion.TIEBA_V11,
    needSToken: Boolean = true,
): MyMultipartBody {
    return MyMultipartBody.Builder(BOUNDARY)
        .apply {
            setType(MyMultipartBody.FORM)
            if (needSToken) {
                val sToken = AccountUtil.getSToken()
                if (sToken != null) addFormDataPart(Param.STOKEN, sToken)
            }
            addFormDataPart(Param.CLIENT_VERSION, clientVersion.version)
            addFormDataPart("data", "file", data.encode().toRequestBody())
        }
        .build()
}

fun buildAdParam(
    load_count: Int = 0,
    refresh_count: Int = 4,
    yoga_lib_version: String? = "1.0"
): AdParam {
    return AdParam(
        load_count = load_count,
        refresh_count = refresh_count,
        yoga_lib_version = yoga_lib_version
    )
}

fun buildAppPosInfo(): AppPosInfo {
    return AppPosInfo(
        addr_timestamp = 0L,
        ap_connected = true,
        ap_mac = "02:00:00:00:00:00",
        asp_shown_info = "",
        coordinate_type = "BD09LL"
    )
}

fun buildCommonRequest(
    context: Context = App.INSTANCE,
    clientVersion: ClientVersion = ClientVersion.TIEBA_V11
): CommonRequest = when (clientVersion) {
    ClientVersion.TIEBA_V11 ->
        CommonRequest(
            BDUSS = AccountUtil.getBduss(),
            _client_id = ClientUtils.clientId ?: RetrofitTiebaApi.randomClientId,
            _client_type = 2,
            _client_version = clientVersion.version,
            _os_version = "${Build.VERSION.SDK_INT}",
            _phone_imei = MobileInfoUtil.getIMEI(context),
            _timestamp = System.currentTimeMillis(),
            brand = Build.BRAND,
            c3_aid = UIDUtil.getAid(),
            cuid = CuidUtils.getNewCuid(),
            cuid_galaxy2 = CuidUtils.getNewCuid(),
            cuid_gid = "",
            from = "1024324o",
            is_teenager = 0,
            lego_lib_version = "3.0.0",
            model = Build.MODEL,
            net_type = "1",
            oaid = OAID().toJson(),
            pversion = "1.0.3",
            sample_id = ClientUtils.sampleId,
            stoken = AccountUtil.getSToken(),
        )

    ClientVersion.TIEBA_V12 ->
        CommonRequest(
            BDUSS = AccountUtil.getBduss(),
            _client_id = ClientUtils.clientId ?: RetrofitTiebaApi.randomClientId,
            _client_type = 2,
            _client_version = clientVersion.version,
            _os_version = "${Build.VERSION.SDK_INT}",
            _phone_imei = MobileInfoUtil.getIMEI(context),
            _timestamp = System.currentTimeMillis(),
            brand = Build.BRAND,
            c3_aid = UIDUtil.getAid(),
            cuid = CuidUtils.getNewCuid(),
            cuid_galaxy2 = CuidUtils.getNewCuid(),
            cuid_gid = "",
            from = "1024324o",
            is_teenager = 0,
            lego_lib_version = "3.0.0",
            model = Build.MODEL,
            net_type = "1",
            oaid = UIDUtil.getOAID(),
            pversion = "1.0.3",
            sample_id = ClientUtils.sampleId,
            stoken = AccountUtil.getSToken(),
        )
}