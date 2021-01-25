package com.huanchengfly.tieba.post.widgets.theme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;

import com.allen.library.SuperTextView;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable;
import com.huanchengfly.tieba.post.ui.theme.utils.ColorStateListUtils;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;

public class TintSuperTextView extends SuperTextView implements Tintable {
    public static final String TAG = "TintSuperTextView";

    private int mBackgroundTintResId;
    private int mLeftTextColorResId;
    private int mLeftTopTextColorResId;
    private int mLeftBottomTextColorResId;
    private int mCenterTextColorResId;
    private int mCenterTopTextColorResId;
    private int mCenterBottomTextColorResId;
    private int mRightTextColorResId;
    private int mRightTopTextColorResId;
    private int mRightBottomTextColorResId;
    private int mDividerLineColorResId;
    private int mLeftIconTintResId;
    private int mRightIconTintResId;

    public TintSuperTextView(Context context) {
        this(context, null);
    }

    public TintSuperTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintSuperTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            mBackgroundTintResId = 0;
            mLeftTextColorResId = R.color.default_color_text;
            mLeftTopTextColorResId = R.color.default_color_text;
            mLeftBottomTextColorResId = R.color.default_color_text;
            mCenterTextColorResId = R.color.default_color_text;
            mCenterTopTextColorResId = R.color.default_color_text;
            mCenterBottomTextColorResId = R.color.default_color_text;
            mRightTextColorResId = R.color.default_color_text;
            mRightTopTextColorResId = R.color.default_color_text;
            mRightBottomTextColorResId = R.color.default_color_text;
            mDividerLineColorResId = R.color.default_color_divider;
            mLeftIconTintResId = 0;
            mRightIconTintResId = 0;
            applyTintColor();
            return;
        }
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TintSuperTextView, defStyleAttr, 0);
        mBackgroundTintResId = array.getResourceId(R.styleable.TintSuperTextView_superTextViewBackgroundTint, 0);
        mLeftTextColorResId = array.getResourceId(R.styleable.TintSuperTextView_leftTextColor, R.color.transparent);
        mLeftTopTextColorResId = array.getResourceId(R.styleable.TintSuperTextView_leftTopTextColor, R.color.transparent);
        mLeftBottomTextColorResId = array.getResourceId(R.styleable.TintSuperTextView_leftBottomTextColor, R.color.transparent);
        mCenterTextColorResId = array.getResourceId(R.styleable.TintSuperTextView_centerTextColor, R.color.transparent);
        mCenterTopTextColorResId = array.getResourceId(R.styleable.TintSuperTextView_centerTopTextColor, R.color.transparent);
        mCenterBottomTextColorResId = array.getResourceId(R.styleable.TintSuperTextView_centerBottomTextColor, R.color.transparent);
        mRightTextColorResId = array.getResourceId(R.styleable.TintSuperTextView_rightTextColor, R.color.transparent);
        mRightTopTextColorResId = array.getResourceId(R.styleable.TintSuperTextView_rightTopTextColor, R.color.transparent);
        mRightBottomTextColorResId = array.getResourceId(R.styleable.TintSuperTextView_rightBottomTextColor, R.color.transparent);
        mDividerLineColorResId = array.getResourceId(R.styleable.TintSuperTextView_dividerLineColor, R.color.transparent);
        mLeftIconTintResId = array.getResourceId(R.styleable.TintSuperTextView_leftIconTint, 0);
        mRightIconTintResId = array.getResourceId(R.styleable.TintSuperTextView_rightIconTint, 0);
        array.recycle();
        applyTintColor();
    }

    private void applyTintColor() {
        if (mBackgroundTintResId != 0) {
            setBackgroundTintList(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mBackgroundTintResId)));
        }
        setLeftTextColor(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mLeftTextColorResId)));
        setLeftTopTextColor(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mLeftTopTextColorResId)));
        setLeftBottomTextColor(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mLeftBottomTextColorResId)));
        setCenterTextColor(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mCenterTextColorResId)));
        setCenterTopTextColor(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mCenterTopTextColorResId)));
        setCenterBottomTextColor(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mCenterBottomTextColorResId)));
        setRightTextColor(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mRightTextColorResId)));
        setRightTopTextColor(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mRightTopTextColorResId)));
        setRightBottomTextColor(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mRightBottomTextColorResId)));
        setBottomDividerLineColor(ThemeUtils.getColorById(getContext(), mDividerLineColorResId));
        if (mLeftIconTintResId != 0) {
            getLeftIconIV().setImageTintList(ColorStateListUtils.createColorStateList(getContext(), mRightTextColorResId));
            Log.i(TAG, "applyTintColor: left");
        }
        if (mRightIconTintResId != 0) {
            getRightIconIV().setImageTintList(ColorStateListUtils.createColorStateList(getContext(), mRightTextColorResId));
            Log.i(TAG, "applyTintColor: right");
        }
    }

    @Override
    public void tint() {
        applyTintColor();
    }
}
