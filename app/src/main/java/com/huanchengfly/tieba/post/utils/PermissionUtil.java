package com.huanchengfly.tieba.post.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.huanchengfly.tieba.post.R;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionUtil {
    public static void askPermission(Context context, Permission... permissions) {
        askPermission(context, null, permissions);
    }

    public static void askPermission(Context context, Action<List<String>> action, Permission... permissions) {
        askPermission(context, action, context.getString(R.string.tip_no_permission), permissions);
    }

    public static void askPermission(Context context, Action<List<String>> action, @StringRes int deniedTip, Permission... permissions) {
        askPermission(context, action, context.getString(deniedTip), permissions);
    }

    private static String getMessage(Context context, Permission... permissions) {
        StringBuilder stringBuilder = new StringBuilder(context.getString(R.string.message_need_permission));
        for (Permission permission : permissions) {
            if (permission.isGroup()) {
                if (!AndPermission.hasPermissions(context, permission.getPermissionGroup())) {
                    stringBuilder.append(context.getString(R.string.message_permission_desc, com.yanzhenjie.permission.runtime.Permission.transformText(context, permission.getPermissionGroup()).get(0), permission.getDesc()));
                }
            } else {
                if (!AndPermission.hasPermissions(context, permission.getPermission())) {
                    stringBuilder.append(context.getString(R.string.message_permission_desc, com.yanzhenjie.permission.runtime.Permission.transformText(context, permission.getPermission()).get(0), permission.getDesc()));
                }
            }
        }
        stringBuilder.append(context.getString(R.string.tip_regrant_permission, context.getString(R.string.button_sure_default)));
        return stringBuilder.toString();
    }

    @SuppressLint("WrongConstant")
    public static void askPermission(Context context, @Nullable Action<List<String>> action, @Nullable String deniedTip, Permission... permissions) {
        List<String> permissionList = new ArrayList<>();
        for (Permission permission : permissions) {
            if (permission.isGroup()) {
                permissionList.addAll(Arrays.asList(permission.getPermissionGroup()));
            } else {
                permissionList.add(permission.getPermission());
            }
        }
        String[] permissionArray = permissionList.toArray(new String[0]);
        if (AndPermission.hasPermissions(context, permissionArray) && action != null) {
            action.onAction(permissionList);
            return;
        }
        AndPermission.with(context)
                .runtime()
                .permission(permissionArray)
                .rationale((c, data, executor) -> {
                    DialogUtil.build(c)
                            .setCancelable(false)
                            .setTitle(R.string.title_dialog_permission)
                            .setMessage(getMessage(c, permissions))
                            .setPositiveButton(R.string.button_sure_default, (dialog, which) -> {
                                executor.execute();
                            })
                            .setNegativeButton(R.string.button_cancel, (dialog, which) -> {
                                executor.cancel();
                            })
                            .create()
                            .show();
                })
                .onGranted(action)
                .onDenied(data -> {
                    if (!TextUtils.isEmpty(deniedTip))
                        Toast.makeText(context, deniedTip, Toast.LENGTH_SHORT).show();
                    boolean hasAlwaysDeniedPermission = AndPermission.hasAlwaysDeniedPermission(context, data);
                    if (hasAlwaysDeniedPermission) {
                        AndPermission.with(context)
                                .runtime()
                                .setting()
                                .start(0);
                    }
                    /*
                    DialogUtil.build(context)
                            .setCancelable(false)
                            .setTitle(R.string.title_dialog_permission)
                            .setMessage(getMessage(context, permissions))
                            .setPositiveButton(R.string.button_sure_default, (dialog, which) -> {
                                if (hasAlwaysDeniedPermission) {
                                    AndPermission.with(context)
                                            .runtime()
                                            .setting()
                                            .onComeback(() -> askPermission(context, action, deniedTip, permissions))
                                            .start();
                                } else {
                                    askPermission(context, action, deniedTip, permissions);
                                }
                            })
                            .setNegativeButton(R.string.button_cancel, (dialog, which) -> {
                                if (!TextUtils.isEmpty(deniedTip))
                                    Toast.makeText(context, deniedTip, Toast.LENGTH_SHORT).show();
                            })
                            .create()
                            .show();
                            */
                })
                .start();
    }

    public static class Permission {
        private boolean isGroup;
        private String permission;
        private String[] permissionGroup;
        private String desc;

        public Permission(String permission, String desc) {
            this.isGroup = false;
            this.permission = permission;
            this.desc = desc;
        }

        public Permission(String[] permissionGroup, String desc) {
            this.isGroup = true;
            this.permissionGroup = permissionGroup;
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }

        public boolean isGroup() {
            return isGroup;
        }

        public String getPermission() {
            return permission;
        }

        public String[] getPermissionGroup() {
            return permissionGroup;
        }
    }
}