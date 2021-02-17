package com.huanchengfly.tieba.post.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.utils.Util;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseBottomSheetDialogFragment extends BottomSheetDialogFragment {
    public static final String TAG = "BaseBottomSheetDialog";
    protected BottomSheetDialog dialog;
    Unbinder mUnbinder;
    View rootView;
    protected BottomSheetBehavior mBehavior;
    private Context attachContext;

    public BaseBottomSheetDialogFragment() {
    }

    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachToContext(context);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
    }

    @CallSuper
    private void onAttachToContext(Context context) {
        attachContext = context;
    }

    @NonNull
    protected Context getAttachContext() {
        return attachContext;
    }

    protected int getScreenHeight() {
        return getAttachContext().getResources().getDisplayMetrics().heightPixels;
    }

    protected int getStatusBarHeight() {
        int statusBarHeight = 0;
        Resources resources = getAttachContext().getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
            statusBarHeight = resources.getDimensionPixelSize(resourceId);
        return statusBarHeight;
    }

    protected boolean isFullScreen() {
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
            bottomSheet.getLayoutParams().height = isFullScreen() ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        final View view = getView();
        if (view != null) {
            view.post(() -> {
                View parent = (View) view.getParent();
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) parent.getLayoutParams();
                CoordinatorLayout.Behavior behavior = params.getBehavior();
                BottomSheetBehavior bottomSheetBehavior = (BottomSheetBehavior) behavior;
                if (bottomSheetBehavior != null)
                    bottomSheetBehavior.setPeekHeight(view.getMeasuredHeight());
            });
        }
    }

    protected abstract void initView();

    public void resetView() {
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    /**
     * 使用关闭弹框 是否使用动画可选
     * 使用动画 同时切换界面Aty会卡顿 建议直接关闭
     */
    public void close() {
        dismiss();
    }

    protected void onCreatedBehavior(BottomSheetBehavior<?> behavior) {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new BottomSheetDialog(getAttachContext(), R.style.BottomSheetDialogStyle);
        if (rootView == null) {
            rootView = Util.inflate(getAttachContext(), getLayoutId());
            assert rootView != null;
            mUnbinder = ButterKnife.bind(this, rootView);
        }
        resetView();
        dialog.setContentView(rootView);
        mBehavior = dialog.getBehavior();
        mBehavior.setHideable(true);
        onCreatedBehavior(mBehavior);
        if (dialog.getWindow() != null) {
            if (needFixHeight())
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, getHeight());
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            ((View) rootView.getParent()).setBackgroundColor(Color.TRANSPARENT);
            dialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundColor(Color.TRANSPARENT);
        }
        initView();
        return dialog;
    }

    protected int getHeight() {
        int screenHeight = getScreenHeight();
        int statusBarHeight = getStatusBarHeight();
        int dialogHeight = screenHeight - statusBarHeight;
        return dialogHeight == 0 ? ViewGroup.LayoutParams.MATCH_PARENT : dialogHeight;
    }

    protected abstract int getLayoutId();

    protected boolean needFixHeight() {
        return true;
    }
}
