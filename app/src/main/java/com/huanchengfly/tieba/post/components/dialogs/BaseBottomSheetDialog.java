package com.huanchengfly.tieba.post.components.dialogs;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.huanchengfly.tieba.post.R;

public abstract class BaseBottomSheetDialog extends BottomSheetDialog {
    public BaseBottomSheetDialog(@NonNull Context context) {
        this(context, R.style.BottomSheetDialogStyle);
    }

    public BaseBottomSheetDialog(@NonNull Context context, int theme) {
        super(context, theme);
        initView();
    }

    protected BaseBottomSheetDialog(@NonNull Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        initView();
    }

    private void initView() {
        View view = getLayoutInflater().inflate(getLayoutId(), null);
        setContentView(view);
        initView(view);
    }

    protected abstract void initView(View contentView);

    abstract int getLayoutId();
}
