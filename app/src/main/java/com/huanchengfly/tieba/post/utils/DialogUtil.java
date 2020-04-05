package com.huanchengfly.tieba.post.utils;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.huanchengfly.tieba.post.R;

public class DialogUtil {
    public static AlertDialog.Builder build(Context context) {
        return new AlertDialog.Builder(context);
    }

    public static AlertDialog.Builder buildBottomDialog(Context context) {
        return new AlertDialog.Builder(context, R.style.Dialog_Bottom);
    }
}