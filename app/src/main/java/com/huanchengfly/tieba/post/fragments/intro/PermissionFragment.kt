package com.huanchengfly.tieba.post.fragments.intro

import android.os.Build
import android.view.ViewGroup
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.intro.fragments.BaseIntroFragment
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.PermissionUtils

class PermissionFragment : BaseIntroFragment() {
    override fun getIconRes(): Int = R.drawable.ic_round_warning
    override fun getTitle(): CharSequence =
        attachContext.getString(R.string.title_fragment_permission)

    override fun getSubtitle(): CharSequence =
        attachContext.getString(R.string.subtitle_fragment_permission)

    override fun getIconColor(): Int = ThemeUtils.getColorByAttr(attachContext, R.attr.colorAccent)
    override fun getTitleTextColor(): Int =
        ThemeUtils.getColorByAttr(attachContext, R.attr.colorText)

    override fun getSubtitleTextColor(): Int =
        ThemeUtils.getColorByAttr(attachContext, R.attr.colorTextSecondary)

    override fun getCustomLayoutResId(): Int = R.layout.layout_fragment_permission

    override fun initCustomLayout(container: ViewGroup) {
        super.initCustomLayout(container)
    }

    override fun onNext(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            PermissionUtils.askPermission(
                attachContext,
                PermissionUtils.Permission(
                    listOf(PermissionUtils.READ_PHONE_STATE),
                    getString(R.string.tip_permission_phone)
                )
            ) { next() }
            return true
        }
        return false
    }
}