package com.huanchengfly.tieba.post.utils

import android.annotation.SuppressLint
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.App.Companion.INSTANCE
import com.huanchengfly.tieba.post.toMD5
import com.huanchengfly.tieba.post.utils.helios.Base32
import com.huanchengfly.tieba.post.utils.helios.Hasher
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.UUID

object UIDUtil {
    @get:SuppressLint("HardwareIds")
    val androidId: String
        get() = getAndroidId("")

    @SuppressLint("HardwareIds")
    fun getAndroidId(defaultValue: String): String {
        val androidId =
            Settings.Secure.getString(INSTANCE.contentResolver, Settings.Secure.ANDROID_ID)
        return androidId ?: defaultValue
    }

    fun getOAID(): String {
        if (App.Config.encodedOAID.isBlank()) return ""
        val raw = "A10-${App.Config.encodedOAID}-"
        val sign = Base32.encode(Hasher.hash(raw.toByteArray()))
        return "$raw$sign"
    }

    fun getAid(): String {
        val raw = "com.helios" + getAndroidId("000000000") + uUID
        val bytes = getSHA1(raw)
        val encoded = Base32.encode(bytes)
        val rawAid = "A00-$encoded-"
        val sign = Base32.encode(Hasher.hash(rawAid.toByteArray()))
        return "$rawAid$sign"
    }

    private fun getSHA1(str: String): ByteArray {
        var sha1: ByteArray = "".toByteArray()
        try {
            val digest = MessageDigest.getInstance("SHA1")
            sha1 = digest.digest(str.toByteArray(StandardCharsets.UTF_8))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sha1
    }

    val newCUID: String
        get() = "baidutiebaapp$uUID"

    val cUID: String
        get() {
            val androidId = androidId
            var imei = MobileInfoUtil.getIMEI(INSTANCE)
            if (TextUtils.isEmpty(imei)) {
                imei = "0"
            }
            val raw =
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) imei + androidId + uUID else "com.baidu$androidId"
            return raw.toMD5().uppercase()
        }

    val finalCUID: String
        get() {
            var imei = MobileInfoUtil.getIMEI(INSTANCE)
            if (TextUtils.isEmpty(imei)) {
                imei = "0"
            }
            return cUID + "|" + StringBuffer(imei).reverse()
        }

    @get:SuppressLint("ApplySharedPref")
    val uUID: String
        get() {
            var uuid = SharedPreferencesUtil.get(INSTANCE, SharedPreferencesUtil.SP_APP_DATA)
                .getString("uuid", null)
            if (uuid == null) {
                uuid = UUID.randomUUID().toString()
                SharedPreferencesUtil.get(INSTANCE, SharedPreferencesUtil.SP_APP_DATA)
                    .edit()
                    .putString("uuid", uuid)
                    .apply()
            }
            return uuid
        }
}