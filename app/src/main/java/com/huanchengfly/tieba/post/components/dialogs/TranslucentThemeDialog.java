package com.huanchengfly.tieba.post.components.dialogs;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import androidx.annotation.NonNull;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.components.MyImageEngine;
import com.huanchengfly.tieba.post.utils.SharedPreferencesUtil;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.huanchengfly.utils.PermissionUtil;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.runtime.Permission;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.util.List;

import static com.huanchengfly.tieba.post.activities.TranslucentThemeActivity.REQUEST_CODE_CHOOSE;

public class TranslucentThemeDialog extends BaseFullScreenDialog implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    public static final String TAG = TranslucentThemeDialog.class.getSimpleName();
    private final Context mContext;

    public TranslucentThemeDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_translucent_theme;
    }

    @Override
    protected void initView(View contentView) {
        Button selectPicBtn = contentView.findViewById(R.id.select_pic);
        SeekBar alphaSeekBar = contentView.findViewById(R.id.alpha);
        SeekBar blueSeekBar = contentView.findViewById(R.id.blur);
        alphaSeekBar.setOnSeekBarChangeListener(this);
        blueSeekBar.setOnSeekBarChangeListener(this);
        selectPicBtn.setOnClickListener(this);
        ThemeUtil.setTranslucentThemeBackground(contentView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_pic:
                askPermission(data -> Matisse.from((Activity) mContext)
                        .choose(MimeType.ofImage())
                        .theme(ThemeUtil.isNightMode(getContext()) ? R.style.Matisse_Dracula : R.style.Matisse_Zhihu)
                        .imageEngine(new MyImageEngine())
                        .forResult(REQUEST_CODE_CHOOSE));
                break;
        }
    }

    private void askPermission(Action<List<String>> granted) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            PermissionUtil.askPermission(getContext(), granted, R.string.toast_no_permission_insert_photo,
                    new PermissionUtil.Permission(Permission.Group.STORAGE, getContext().getString(R.string.tip_permission_storage)));
        } else {
            PermissionUtil.askPermission(getContext(), granted, R.string.toast_no_permission_insert_photo,
                    new PermissionUtil.Permission(Permission.READ_EXTERNAL_STORAGE, getContext().getString(R.string.tip_permission_storage)));
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.alpha:
                SharedPreferencesUtil.put(getContext(), SharedPreferencesUtil.SP_SETTINGS, "translucent_background_alpha", progress);
                break;
            case R.id.blur:
                SharedPreferencesUtil.put(getContext(), SharedPreferencesUtil.SP_SETTINGS, "translucent_background_blur", progress);
                break;
        }
        ThemeUtil.setTranslucentThemeBackground(getContentView());
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
