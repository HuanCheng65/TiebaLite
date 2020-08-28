package com.huanchengfly.tieba.post.components.dialogs;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.huanchengfly.tieba.post.R;

public class LoadingDialog extends AlertDialog {
    private Context mContext;
    private TextView loadingTipView;

    public LoadingDialog(Context context) {
        super(context);
        this.mContext = context;
        initView();
    }

    private void initView() {
        View contentView = View.inflate(mContext, R.layout.dialog_loading, null);
        loadingTipView = contentView.findViewById(R.id.dialog_loading_tip);
        setCancelable(false);
        setView(contentView);
        setTipText(R.string.text_loading);
    }

    public void setTipText(@StringRes int resId) {
        loadingTipView.setText(resId);
    }

    public void setTipText(String text) {
        loadingTipView.setText(text);
    }
}
