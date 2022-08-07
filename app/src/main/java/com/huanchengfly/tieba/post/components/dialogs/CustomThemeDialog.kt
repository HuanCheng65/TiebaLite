package com.huanchengfly.tieba.post.components.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import com.huanchengfly.tieba.post.BaseApplication.ThemeDelegate.getColorByAttr
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.appPreferences
import com.jrummyapps.android.colorpicker.ColorPickerDialog
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener

class CustomThemeDialog(context: Context) : AlertDialog(context),
    View.OnClickListener, DialogInterface.OnClickListener, CompoundButton.OnCheckedChangeListener,
    ColorPickerDialogListener {
    private var primaryColorLayout: LinearLayout? = null
    private var primaryColorView: View? = null
    private var statusBarFont: CheckBox? = null
    private var toolbarPrimaryColor: CheckBox? = null
    private var primaryColor = 0
    private var statusBarFontDark = false
    private var toolbarPrimary = false
    private fun initListener() {
        primaryColorLayout!!.setOnClickListener(this)
        statusBarFont!!.setOnCheckedChangeListener(this)
        toolbarPrimaryColor!!.setOnCheckedChangeListener(this)
    }

    private fun initView() {
        val contentView = View.inflate(context, R.layout.dialog_custom_theme, null)
        primaryColorLayout = contentView.findViewById(R.id.custom_theme_primary_holder)
        primaryColorView = contentView.findViewById(R.id.custom_theme_primary)
        statusBarFont = contentView.findViewById(R.id.custom_theme_status_bar_font)
        toolbarPrimaryColor = contentView.findViewById(R.id.custom_theme_toolbar_primary_color)
        setView(contentView)
        primaryColor = getColorByAttr(context, R.attr.colorPrimary, ThemeUtil.THEME_CUSTOM)
        statusBarFontDark = context.appPreferences.customStatusBarFontDark
        toolbarPrimary = context.appPreferences.toolbarPrimaryColor
        refreshView()
    }

    private fun refreshView() {
        primaryColorView!!.backgroundTintList = ColorStateList.valueOf(primaryColor)
        statusBarFont!!.isChecked = statusBarFontDark
        toolbarPrimaryColor!!.isChecked = toolbarPrimary
        statusBarFont!!.visibility = if (toolbarPrimary) View.VISIBLE else View.GONE
        ThemeUtils.refreshUI(context)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.custom_theme_primary_holder) {
            val primaryColorPicker = ColorPickerDialog.newBuilder()
                .setDialogTitle(R.string.title_color_picker_primary)
                .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                .setShowAlphaSlider(false)
                .setDialogId(0)
                .setAllowPresets(true)
                .setColor(primaryColor)
                .create()
            primaryColorPicker.setColorPickerDialogListener(this)
            primaryColorPicker.show(
                ThemeUtils.getWrapperActivity(context)!!.fragmentManager,
                "ColorPicker_PrimaryColor"
            )
        }
        refreshView()
    }

    @SuppressLint("ApplySharedPref")
    override fun onClick(dialog: DialogInterface, which: Int) {
        context.appPreferences.apply {
            customPrimaryColor = toString(primaryColor)
            customStatusBarFontDark = (statusBarFontDark || !toolbarPrimary)
            toolbarPrimaryColor = toolbarPrimary
        }
        dialog.dismiss()
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.id == R.id.custom_theme_status_bar_font) {
            statusBarFontDark = isChecked
        } else if (buttonView.id == R.id.custom_theme_toolbar_primary_color) {
            toolbarPrimary = isChecked
            statusBarFontDark = !isChecked
        }
        refreshView()
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        if (dialogId == 0) {
            primaryColor = color
            refreshView()
        }
    }

    override fun onDialogDismissed(dialogId: Int) {}

    companion object {
        fun toString(alpha: Int, red: Int, green: Int, blue: Int): String {
            val hr = Integer.toHexString(red)
            val hg = Integer.toHexString(green)
            val hb = Integer.toHexString(blue)
            val ha = Integer.toHexString(alpha)
            return "#" + fixHexString(ha) + fixHexString(hr) + fixHexString(hg) + fixHexString(hb)
        }

        private fun fixHexString(hex: String): String {
            var hexString = hex
            if (hexString.isEmpty()) {
                hexString = "00"
            }
            if (hexString.length == 1) {
                hexString = "0$hexString"
            }
            if (hexString.length > 2) {
                hexString = hexString.substring(0, 2)
            }
            return hexString
        }

        fun toString(@ColorInt color: Int): String {
            return toString(
                Color.alpha(color),
                Color.red(color),
                Color.green(color),
                Color.blue(color)
            )
        }
    }

    init {
        setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.button_finish), this)
        setCancelable(false)
        setTitle(R.string.title_custom_theme)
        initView()
        initListener()
    }
}