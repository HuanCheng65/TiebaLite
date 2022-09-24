package com.huanchengfly.tieba.post.ui.widgets.theme;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.common.theme.interfaces.Tintable;
import com.huanchengfly.tieba.post.ui.common.theme.utils.ColorStateListUtils;
import com.huanchengfly.tieba.post.ui.widgets.edittext.widget.UndoableEditText;

public class TintUndoableEditText extends UndoableEditText implements Tintable {
    private int mTextColorResId;
    private int mHintTextColorResId;

    public TintUndoableEditText(Context context) {
        super(context);
        init(null, 0);
    }

    public TintUndoableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public TintUndoableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            mTextColorResId = 0;
            mHintTextColorResId = 0;
            tint();
            return;
        }
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TintUndoableEditText, defStyleAttr, 0);
        mTextColorResId = array.getResourceId(R.styleable.TintUndoableEditText_textColor, 0);
        mHintTextColorResId = array.getResourceId(R.styleable.TintUndoableEditText_hintTextColor, 0);
        array.recycle();
        tint();
    }

    @Override
    public void tint() {
        if (mTextColorResId != 0) {
            setTextColor(ColorStateListUtils.createColorStateList(getContext(), mTextColorResId));
        }
        if (mHintTextColorResId != 0) {
            setHintTextColor(ColorStateListUtils.createColorStateList(getContext(), mHintTextColorResId));
        }
    }
}
