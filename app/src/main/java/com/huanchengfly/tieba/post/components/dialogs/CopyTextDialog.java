package com.huanchengfly.tieba.post.components.dialogs;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.huanchengfly.tieba.post.R;

public class CopyTextDialog extends BaseFullScreenDialog {
    public static final String TAG = CopyTextDialog.class.getSimpleName();

    private final String mText;

    public CopyTextDialog(@NonNull Context context, @NonNull String text) {
        super(context);
        this.mText = text;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_copy_text;
    }

    @Override
    protected void initView(View contentView) {
        TextView textView = contentView.findViewById(R.id.dialog_copy_text);
        textView.setText(mText);
        textView.setTextIsSelectable(true);
    }
}