package com.huanchengfly.tieba.post.components.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.BaseApplication;
import com.huanchengfly.tieba.post.utils.SharedPreferencesUtil;
import com.jrummyapps.android.colorpicker.ColorPickerDialog;
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener;

import java.util.Objects;

import static com.huanchengfly.tieba.post.utils.ThemeUtil.SP_CUSTOM_PRIMARY_COLOR;
import static com.huanchengfly.tieba.post.utils.ThemeUtil.SP_CUSTOM_STATUS_BAR_FONT_DARK;
import static com.huanchengfly.tieba.post.utils.ThemeUtil.SP_CUSTOM_TOOLBAR_PRIMARY_COLOR;
import static com.huanchengfly.tieba.post.utils.ThemeUtil.THEME_CUSTOM;

public class CustomThemeDialog extends AlertDialog implements View.OnClickListener, DialogInterface.OnClickListener, CompoundButton.OnCheckedChangeListener, ColorPickerDialogListener {
    private LinearLayout primaryColorLayout;
    private View primaryColorView;
    private CheckBox statusBarFont;
    private CheckBox toolbarPrimaryColor;
    private int primaryColor;
    private boolean statusBarFontDark;
    private boolean toolbarPrimary;

    public CustomThemeDialog(@NonNull Context context) {
        super(context);
        setButton(BUTTON_POSITIVE, context.getString(R.string.button_finish), this);
        setCancelable(false);
        setTitle(R.string.title_custom_theme);
        initView();
        initListener();
    }

    public static String toString(int alpha, int red, int green, int blue) {
        String hr = Integer.toHexString(red);
        String hg = Integer.toHexString(green);
        String hb = Integer.toHexString(blue);
        String ha = Integer.toHexString(alpha);
        return "#" + fixHexString(ha) + fixHexString(hr) + fixHexString(hg) + fixHexString(hb);
    }

    public static String fixHexString(String hexString) {
        if (hexString.length() < 1) {
            hexString = "00";
        }
        if (hexString.length() == 1) {
            hexString = "0" + hexString;
        }
        if (hexString.length() > 2) {
            hexString = hexString.substring(0, 2);
        }
        return hexString;
    }

    public static String toString(@ColorInt int color) {
        return toString(Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color));
    }

    private void initListener() {
        primaryColorLayout.setOnClickListener(this);
        statusBarFont.setOnCheckedChangeListener(this);
        toolbarPrimaryColor.setOnCheckedChangeListener(this);
    }

    private void initView() {
        View contentView = View.inflate(getContext(), R.layout.dialog_custom_theme, null);
        primaryColorLayout = contentView.findViewById(R.id.custom_theme_primary_holder);
        primaryColorView = contentView.findViewById(R.id.custom_theme_primary);
        statusBarFont = contentView.findViewById(R.id.custom_theme_status_bar_font);
        toolbarPrimaryColor = contentView.findViewById(R.id.custom_theme_toolbar_primary_color);
        setView(contentView);
        primaryColor = BaseApplication.ThemeDelegate.INSTANCE.getColorByAttr(getContext(), R.attr.colorPrimary, THEME_CUSTOM);
        statusBarFontDark = SharedPreferencesUtil.get(getContext(), SharedPreferencesUtil.SP_SETTINGS)
                .getBoolean(SP_CUSTOM_STATUS_BAR_FONT_DARK, false);
        toolbarPrimary = SharedPreferencesUtil.get(getContext(), SharedPreferencesUtil.SP_SETTINGS)
                .getBoolean(SP_CUSTOM_TOOLBAR_PRIMARY_COLOR, true);
        refreshView();
    }

    private void refreshView() {
        primaryColorView.setBackgroundTintList(ColorStateList.valueOf(primaryColor));
        statusBarFont.setChecked(statusBarFontDark);
        toolbarPrimaryColor.setChecked(toolbarPrimary);
        statusBarFont.setVisibility(toolbarPrimary ? View.VISIBLE : View.GONE);
        ThemeUtils.refreshUI(getContext());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.custom_theme_primary_holder) {
            ColorPickerDialog primaryColorPicker = ColorPickerDialog.newBuilder()
                    .setDialogTitle(R.string.title_color_picker_primary)
                    .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                    .setShowAlphaSlider(false)
                    .setDialogId(0)
                    .setAllowPresets(true)
                    .setColor(primaryColor)
                    .create();
            primaryColorPicker.setColorPickerDialogListener(this);
            primaryColorPicker.show(Objects.requireNonNull(ThemeUtils.getWrapperActivity(getContext())).getFragmentManager(), "ColorPicker_PrimaryColor");
        }
        refreshView();
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void onClick(DialogInterface dialog, int which) {
        SharedPreferencesUtil.get(getContext(), SharedPreferencesUtil.SP_SETTINGS)
                .edit()
                .putString(SP_CUSTOM_PRIMARY_COLOR, toString(primaryColor))
                .putBoolean(SP_CUSTOM_STATUS_BAR_FONT_DARK, statusBarFontDark || !toolbarPrimary)
                .putBoolean(SP_CUSTOM_TOOLBAR_PRIMARY_COLOR, toolbarPrimary)
                .commit();
        dialog.dismiss();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.custom_theme_status_bar_font) {
            statusBarFontDark = isChecked;
        } else if (buttonView.getId() == R.id.custom_theme_toolbar_primary_color) {
            toolbarPrimary = isChecked;
            statusBarFontDark = !isChecked;
        }
        refreshView();
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        if (dialogId == 0) {
            primaryColor = color;
            refreshView();
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {
    }
}
