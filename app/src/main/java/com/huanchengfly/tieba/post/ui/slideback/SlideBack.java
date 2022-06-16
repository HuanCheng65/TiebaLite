package com.huanchengfly.tieba.post.ui.slideback;

import android.app.Activity;

import androidx.annotation.NonNull;

public class SlideBack {
    private ISlideView slideView;   //样式
    private OnSlide onSlide;        //滑动监听
    private int canSlideWidth;      //左边触发距离

    public static SlideBack create() {
        return new SlideBack();
    }

    /**
     * 滑动返回样式
     *
     * @param slideView the slide view
     * @return the slide back
     */
    public SlideBack slideView(ISlideView slideView) {
        this.slideView = slideView;
        return this;
    }

    /**
     * 左边开始触发距离
     *
     * @param canSlideWidth the can slide width
     * @return the slide back
     */
    public SlideBack canSlideWidth(int canSlideWidth) {
        this.canSlideWidth = canSlideWidth;
        return this;
    }

    /**
     * 滑动触发监听
     *
     * @param onSlide the on slide
     * @return the slide back
     */
    public SlideBack onSlide(OnSlide onSlide) {
        this.onSlide = onSlide;
        return this;
    }


    public SlideControlLayout attachToActivity(@NonNull Activity activity) {
        if (slideView == null) {
            slideView = new DefaultSlideView(activity);
        }

        if (canSlideWidth == 0) {
            canSlideWidth = Utils.d2p(activity, 18);
        }

        return new SlideControlLayout(activity, canSlideWidth, slideView, onSlide)
                .attachToActivity(activity);
    }
}
