package com.huanchengfly.tieba.post.components.dialogs;

import android.content.Context;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.textfield.TextInputLayout;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.adapters.TextWatcherAdapter;
import com.huanchengfly.tieba.post.interfaces.OnSubmitListener;
import com.huanchengfly.tieba.post.utils.ImageUtil;

public class VerifyCodeDialog extends AlertDialog {
    private final Context mContext;
    private View contentView;
    private ImageView codeImageView;
    private Button submitBtn;
    private TextInputLayout codeInputLayout;
    private EditText codeInputEditText;
    private final String picUrl;
    private OnSubmitListener onSubmitListener;

    public VerifyCodeDialog(Context context, String picUrl) {
        super(context);
        this.mContext = context;
        this.picUrl = picUrl;
        setTitle(R.string.title_verify_code);
        initView();
    }

    public VerifyCodeDialog setOnSubmitListener(OnSubmitListener listener) {
        this.onSubmitListener = listener;
        return this;
    }

    private void initView() {
        contentView = View.inflate(mContext, R.layout.dialog_verify_code, null);
        codeImageView = contentView.findViewById(R.id.dialog_verify_code_pic);
        codeInputLayout = contentView.findViewById(R.id.dialog_verify_code_input_layout);
        codeInputEditText = codeInputLayout.getEditText();
        submitBtn = contentView.findViewById(R.id.dialog_vertify_code_submit);
        submitBtn.setOnClickListener((View view) -> {
            if (codeInputEditText.getText() != null) {
                if (!codeInputEditText.getText().toString().isEmpty()) {
                    if (onSubmitListener != null) {
                        onSubmitListener.onSubmit(codeInputEditText.getText().toString());
                    }
                    cancel();
                    return;
                }
            }
            codeInputLayout.setError(getContext().getText(R.string.toast_verify_code_empty));
        });
        codeInputEditText.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable != null) {
                    if (!editable.toString().isEmpty()) {
                        codeInputLayout.setErrorEnabled(false);
                        codeInputLayout.setError(null);
                        return;
                    }
                }
                codeInputLayout.setError(getContext().getText(R.string.toast_verify_code_empty));
            }
        });
        setView(contentView);
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.bg_placeholder)
                .skipMemoryCache(true);
        ImageUtil.load(codeImageView, ImageUtil.LOAD_TYPE_SMALL_PIC, picUrl, true);
    }
}