package com.huanchengfly.tieba.post.ui.widgets.theme;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.common.theme.interfaces.Tintable;
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils;

public class TintSwipeRefreshLayout extends SwipeRefreshLayout implements Tintable {
    public TintSwipeRefreshLayout(@NonNull Context context) {
        this(context, null);
    }

    public TintSwipeRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            return;
        }
        applyTintColor();
    }

    private void applyTintColor() {
        setColorSchemeColors(ThemeUtils.getColorByAttr(getContext(), R.attr.colorAccent));
        setProgressBackgroundColorSchemeColor(ThemeUtils.getColorByAttr(getContext(), R.attr.colorIndicator));
    }

    @Override
    public void tint() {
        applyTintColor();
    }
}
