package com.huanchengfly.tieba.post.components.dialogs;

import android.content.Context;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputLayout;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.adapters.TextWatcherAdapter;
import com.huanchengfly.tieba.post.interfaces.OnSubmitListener;

public class EditTextDialog extends AlertDialog {
    private Context mContext;
    private View contentView;
    private ImageView codeImageView;
    private Button submitBtn;
    private TextInputLayout textInputLayout;
    private EditText editText;
    private OnSubmitListener onSubmitListener;

    public EditTextDialog(Context context) {
        super(context);
        this.mContext = context;
        setTitle(R.string.title_input);
        initView();
    }

    public EditTextDialog setOnSubmitListener(OnSubmitListener listener) {
        this.onSubmitListener = listener;
        return this;
    }

    public EditTextDialog setInputType(int inputType) {
        editText.setInputType(inputType);
        return this;
    }

    public EditTextDialog setTipText(@StringRes int res) {
        return setTipText(mContext.getString(res));
    }

    public EditTextDialog setTipText(CharSequence charSequence) {
        textInputLayout.setHint(charSequence);
        return this;
    }

    public EditTextDialog setHelperText(@StringRes int res) {
        return setHelperText(mContext.getString(res));
    }

    public EditTextDialog setHelperText(CharSequence charSequence) {
        textInputLayout.setHelperText(charSequence);
        return this;
    }

    private void initView() {
        contentView = View.inflate(mContext, R.layout.dialog_edit_text, null);
        textInputLayout = contentView.findViewById(R.id.dialog_edit_text_layout);
        editText = textInputLayout.getEditText();
        submitBtn = contentView.findViewById(R.id.dialog_edit_text_submit);
        Button cancelBtn = contentView.findViewById(R.id.dialog_edit_text_cancel);
        cancelBtn.setOnClickListener(v -> {
            cancel();
        });
        submitBtn.setOnClickListener(view -> {
            if (editText.getText() != null) {
                if (!editText.getText().toString().isEmpty()) {
                    if (onSubmitListener != null) {
                        onSubmitListener.onSubmit(editText.getText().toString());
                    }
                    cancel();
                    return;
                }
            }
            textInputLayout.setError(getContext().getText(R.string.toast_verify_code_empty));
        });
        editText.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable != null) {
                    if (!editable.toString().isEmpty()) {
                        textInputLayout.setErrorEnabled(false);
                        textInputLayout.setError(null);
                        return;
                    }
                }
                textInputLayout.setError(getContext().getText(R.string.toast_verify_code_empty));
            }
        });
        setView(contentView);
    }
}