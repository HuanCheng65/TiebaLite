package com.huanchengfly.tieba.post.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.huanchengfly.tieba.post.R;

public class ConfirmDialogFragment extends MenuDialogFragment {
    private OnConfirmListener onConfirmListener;
    private OnCancelListener onCancelListener;

    public static ConfirmDialogFragment newInstance(String title) {
        ConfirmDialogFragment fragment = new ConfirmDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("menuRes", R.menu.menu_confirm_dialog);
        bundle.putString("title", title);
        fragment.setArguments(bundle);
        return fragment;
    }

    public OnConfirmListener getOnConfirmListener() {
        return onConfirmListener;
    }

    public ConfirmDialogFragment setOnConfirmListener(OnConfirmListener onConfirmListener) {
        this.onConfirmListener = onConfirmListener;
        return this;
    }

    public OnCancelListener getOnCancelListener() {
        return onCancelListener;
    }

    public ConfirmDialogFragment setOnCancelListener(OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.yes:
                    if (getOnConfirmListener() != null) {
                        getOnConfirmListener().onConfirm();
                    }
                    return true;
                case R.id.no:
                    if (getOnCancelListener() != null) {
                        getOnCancelListener().onCancel();
                    }
                    return true;
            }
            return false;
        });
    }

    public interface OnConfirmListener {
        void onConfirm();
    }

    public interface OnCancelListener {
        void onCancel();
    }
}
