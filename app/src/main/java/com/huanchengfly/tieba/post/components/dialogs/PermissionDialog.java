package com.huanchengfly.tieba.post.components.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.interfaces.OnDeniedCallback;
import com.huanchengfly.tieba.post.interfaces.OnGrantedCallback;
import com.huanchengfly.tieba.post.models.PermissionBean;
import com.huanchengfly.tieba.post.utils.SharedPreferencesUtil;

public class PermissionDialog extends AlertDialog implements View.OnClickListener {
    public static final int STATE_DENIED = 2;
    public static final int STATE_ALLOW = 1;
    public static final int STATE_UNSET = 0;
    private TextView titleView;
    private ImageView iconView;
    private Button allowBtn;
    private Button deniedBtn;
    private CheckBox checkBox;
    private PermissionBean permissionBean;
    private OnGrantedCallback onGrantedCallback;
    private OnDeniedCallback onDeniedCallback;

    public PermissionDialog(@NonNull Context context, @NonNull PermissionBean permissionBean) {
        super(context, false, null);
        View contentView = View.inflate(context, R.layout.dialog_permission, null);
        setPermissionBean(permissionBean);
        titleView = contentView.findViewById(R.id.permission_title);
        iconView = contentView.findViewById(R.id.permission_icon);
        allowBtn = contentView.findViewById(R.id.permission_actions_allow);
        deniedBtn = contentView.findViewById(R.id.permission_actions_denied);
        checkBox = contentView.findViewById(R.id.permission_actions_checkbox);
        setView(contentView);
        initView();
    }

    public PermissionBean getPermissionBean() {
        return permissionBean;
    }

    public PermissionDialog setPermissionBean(PermissionBean permissionBean) {
        this.permissionBean = permissionBean;
        return this;
    }

    public OnGrantedCallback getOnGrantedCallback() {
        return onGrantedCallback;
    }

    public PermissionDialog setOnGrantedCallback(OnGrantedCallback onGrantedCallback) {
        this.onGrantedCallback = onGrantedCallback;
        return this;
    }

    public OnDeniedCallback getOnDeniedCallback() {
        return onDeniedCallback;
    }

    public PermissionDialog setOnDeniedCallback(OnDeniedCallback onDeniedCallback) {
        this.onDeniedCallback = onDeniedCallback;
        return this;
    }

    private void initView() {
        if (permissionBean == null) {
            throw new IllegalArgumentException();
        }
        titleView.setText(permissionBean.getTitle());
        iconView.setImageResource(permissionBean.getIcon());
        allowBtn.setOnClickListener(this);
        deniedBtn.setOnClickListener(this);
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.permission_actions_allow:
                if (getOnGrantedCallback() != null) {
                    getOnGrantedCallback().onGranted(checkBox.isChecked());
                }
                if (checkBox.isChecked()) {
                    SharedPreferencesUtil.get(v.getContext(), SharedPreferencesUtil.SP_PERMISSION)
                            .edit()
                            .putInt(permissionBean.getData() + "_" + permissionBean.getId(), STATE_ALLOW)
                            .commit();
                }
                dismiss();
                break;
            case R.id.permission_actions_denied:
                if (getOnDeniedCallback() != null) {
                    getOnDeniedCallback().onDenied(checkBox.isChecked());
                }
                if (checkBox.isChecked()) {
                    SharedPreferencesUtil.get(v.getContext(), SharedPreferencesUtil.SP_PERMISSION)
                            .edit()
                            .putInt(permissionBean.getData() + "_" + permissionBean.getId(), STATE_DENIED)
                            .commit();
                }
                dismiss();
                break;
        }
    }

    @Override
    public void show() {
        int state = SharedPreferencesUtil.get(getContext(), SharedPreferencesUtil.SP_PERMISSION).getInt(permissionBean.getData() + "_" + permissionBean.getId(), STATE_UNSET);
        if (state != STATE_UNSET) {
            if (state == STATE_ALLOW) {
                if (getOnGrantedCallback() != null) {
                    getOnGrantedCallback().onGranted(true);
                }
            } else if (state == STATE_DENIED) {
                if (getOnDeniedCallback() != null) {
                    getOnDeniedCallback().onDenied(true);
                }
            }
        } else {
            super.show();
        }
    }

    public static class CustomPermission {
        public static final int PERMISSION_LOCATION = 0;
        public static final int PERMISSION_START_APP = 1;
        public static final int PERMISSION_CLIPBOARD_COPY = 2;
    }
}