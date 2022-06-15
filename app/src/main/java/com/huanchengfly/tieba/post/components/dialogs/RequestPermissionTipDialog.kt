package com.huanchengfly.tieba.post.components.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.view.setPadding
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.dpToPx
import com.huanchengfly.tieba.post.utils.PermissionUtils


class RequestPermissionTipDialog(context: Context, permission: PermissionUtils.Permission) :
    AlertDialog(context, R.style.Dialog_RequestPermissionTip) {
    val title: TextView
    val message: TextView

    init {
        setCancelable(false)
        setView(View.inflate(context, R.layout.dialog_request_permission_tip, null).also {
            title = it.findViewById(R.id.request_permission_tip_dialog_title)
            message = it.findViewById(R.id.request_permission_tip_dialog_message)
        })
        val permissionName = PermissionUtils.transformText(context, permission.permissions).first()
        title.text = context.getString(R.string.title_request_permission_tip_dialog, permissionName)
        message.text =
            context.getString(R.string.message_request_permission_tip_dialog, permission.desc)
    }

    override fun show() {
        super.show()
        window?.let {
            it.attributes = it.attributes.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
                it.decorView.setPadding(16f.dpToPx())
            }
            it.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL)
        }
    }
}