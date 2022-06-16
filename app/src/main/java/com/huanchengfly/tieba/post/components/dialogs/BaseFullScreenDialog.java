package com.huanchengfly.tieba.post.components.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.huanchengfly.tieba.post.R;

import java.util.Objects;

public abstract class BaseFullScreenDialog extends Dialog {
    private final View mContentView;

    BaseFullScreenDialog(@NonNull Context context) {
        super(context, R.style.Dialog_FullScreen);
        mContentView = View.inflate(getContext(), getLayoutId(), null);
    }

    public View getContentView() {
        return mContentView;
    }

    protected abstract int getLayoutId();

    protected abstract void initView(View contentView);

    @Override
    public void show() {
        super.show();
        WindowManager.LayoutParams layoutParams = Objects.requireNonNull(getWindow()).getAttributes();
        layoutParams.gravity = Gravity.BOTTOM;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().getDecorView().setPadding(0, 0, 0, 0);
        getWindow().setAttributes(layoutParams);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getWindow()).setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(mContentView);
        initView(mContentView);
    }
}
