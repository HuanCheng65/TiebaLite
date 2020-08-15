package com.huanchengfly.tieba.post.ui.overscroll.adapters;

import android.view.View;

import com.huanchengfly.tieba.post.ui.overscroll.HorizontalOverScrollBounceEffectDecorator;
import com.huanchengfly.tieba.post.ui.overscroll.VerticalOverScrollBounceEffectDecorator;

/**
 * A static mAdapter for views that are ALWAYS over-scroll-able (e.g. image view).
 *
 * @author amit
 *
 * @see HorizontalOverScrollBounceEffectDecorator
 * @see VerticalOverScrollBounceEffectDecorator
 */
public class StaticOverScrollDecorAdapter implements IOverScrollDecoratorAdapter {

    protected final View mView;

    public StaticOverScrollDecorAdapter(View view) {
        mView = view;
    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    public boolean isInAbsoluteStart() {
        return true;
    }

    @Override
    public boolean isInAbsoluteEnd() {
        return true;
    }
}
