package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.os.Build
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.components.dialogs.RequestPermissionTipDialog
import com.huanchengfly.tieba.post.toastShort


object PermissionUtils {
    const val READ_CALENDAR = "android.permission.READ_CALENDAR"
    const val WRITE_CALENDAR = "android.permission.WRITE_CALENDAR"

    const val CAMERA = "android.permission.CAMERA"

    const val READ_CONTACTS = "android.permission.READ_CONTACTS"
    const val WRITE_CONTACTS = "android.permission.WRITE_CONTACTS"
    const val GET_ACCOUNTS = "android.permission.GET_ACCOUNTS"

    const val ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION"
    const val ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION"
    const val ACCESS_BACKGROUND_LOCATION = "android.permission.ACCESS_BACKGROUND_LOCATION"

    const val RECORD_AUDIO = "android.permission.RECORD_AUDIO"

    const val READ_PHONE_STATE = "android.permission.READ_PHONE_STATE"
    const val CALL_PHONE = "android.permission.CALL_PHONE"
    const val USE_SIP = "android.permission.USE_SIP"
    const val READ_PHONE_NUMBERS = "android.permission.READ_PHONE_NUMBERS"
    const val ANSWER_PHONE_CALLS = "android.permission.ANSWER_PHONE_CALLS"
    const val ADD_VOICEMAIL = "com.android.voicemail.permission.ADD_VOICEMAIL"

    const val READ_CALL_LOG = "android.permission.READ_CALL_LOG"
    const val WRITE_CALL_LOG = "android.permission.WRITE_CALL_LOG"
    const val PROCESS_OUTGOING_CALLS = "android.permission.PROCESS_OUTGOING_CALLS"

    const val BODY_SENSORS = "android.permission.BODY_SENSORS"
    const val ACTIVITY_RECOGNITION = "android.permission.ACTIVITY_RECOGNITION"

    const val SEND_SMS = "android.permission.SEND_SMS"
    const val RECEIVE_SMS = "android.permission.RECEIVE_SMS"
    const val READ_SMS = "android.permission.READ_SMS"
    const val RECEIVE_WAP_PUSH = "android.permission.RECEIVE_WAP_PUSH"
    const val RECEIVE_MMS = "android.permission.RECEIVE_MMS"

    const val READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE"
    const val WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE"

    /**
     * Turn permissions into text.
     */
    fun transformText(context: Context, permissions: List<String>): List<String> {
        val textList: MutableList<String> = ArrayList()
        for (permission in permissions) {
            when (permission) {
                READ_CALENDAR, WRITE_CALENDAR -> {
                    val message = context.getString(R.string.permission_name_calendar)
                    if (!textList.contains(message)) {
                        textList.add(message)
                    }
                }
                CAMERA -> {
                    val message = context.getString(R.string.permission_name_camera)
                    if (!textList.contains(message)) {
                        textList.add(message)
                    }
                }
                GET_ACCOUNTS, READ_CONTACTS, WRITE_CONTACTS -> {
                    val message = context.getString(R.string.permission_name_contacts)
                    if (!textList.contains(message)) {
                        textList.add(message)
                    }
                }
                ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION -> {
                    val message = context.getString(R.string.permission_name_location)
                    if (!textList.contains(message)) {
                        textList.add(message)
                    }
                }
                RECORD_AUDIO -> {
                    val message = context.getString(R.string.permission_name_microphone)
                    if (!textList.contains(message)) {
                        textList.add(message)
                    }
                }
                READ_PHONE_STATE, CALL_PHONE, ADD_VOICEMAIL, USE_SIP, READ_PHONE_NUMBERS, ANSWER_PHONE_CALLS -> {
                    val message = context.getString(R.string.permission_name_phone)
                    if (!textList.contains(message)) {
                        textList.add(message)
                    }
                }
                READ_CALL_LOG, WRITE_CALL_LOG, PROCESS_OUTGOING_CALLS -> {
                    val messageId: Int =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) R.string.permission_name_call_log else R.string.permission_name_phone
                    val message = context.getString(messageId)
                    if (!textList.contains(message)) {
                        textList.add(message)
                    }
                }
                BODY_SENSORS -> {
                    val message = context.getString(R.string.permission_name_sensors)
                    if (!textList.contains(message)) {
                        textList.add(message)
                    }
                }
                ACTIVITY_RECOGNITION -> {
                    val message = context.getString(R.string.permission_name_activity_recognition)
                    if (!textList.contains(message)) {
                        textList.add(message)
                    }
                }
                SEND_SMS, RECEIVE_SMS, READ_SMS, RECEIVE_WAP_PUSH, RECEIVE_MMS -> {
                    val message = context.getString(R.string.permission_name_sms)
                    if (!textList.contains(message)) {
                        textList.add(message)
                    }
                }
                READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE -> {
                    val message = context.getString(R.string.permission_name_storage)
                    if (!textList.contains(message)) {
                        textList.add(message)
                    }
                }
            }
        }
        return textList
    }

    fun askPermission(context: Context, permission: Permission, onGranted: () -> Unit) {
        askPermission(context, permission, R.string.tip_no_permission, onGranted)
    }

    fun askPermission(
        context: Context,
        permission: Permission,
        deniedToast: Int,
        onGranted: () -> Unit
    ) {
        askPermission(context, permission, context.getString(deniedToast), onGranted, null)
    }

    fun askPermission(
        context: Context,
        permission: Permission,
        deniedToast: Int,
        onGranted: () -> Unit,
        onDenied: (() -> Unit)?
    ) {
        askPermission(context, permission, context.getString(deniedToast), onGranted, onDenied)
    }

    @JvmOverloads
    fun askPermission(
        context: Context,
        permission: Permission,
        deniedToast: String,
        onGranted: () -> Unit,
        onDenied: (() -> Unit)? = null
    ) {
        if (XXPermissions.isGranted(context, permission.permissions)) {
            onGranted()
        } else {
            val dialog = RequestPermissionTipDialog(context, permission)
            XXPermissions.with(context)
                .permission(permission.permissions)
                .request(object : OnPermissionCallback {
                    override fun onGranted(permissions: List<String>, all: Boolean) {
                        if (all) {
                            onGranted()
                        } else {
                            context.toastShort(deniedToast)
                            onDenied?.invoke()
                        }
                        dialog.dismiss()
                    }

                    override fun onDenied(permissions: List<String>, never: Boolean) {
                        context.toastShort(deniedToast)
                        onDenied?.invoke()
                        dialog.dismiss()
                    }
                })
            dialog.show()
        }
    }

    data class Permission(val permissions: List<String>, val desc: String)
}
