package com.huanchengfly.tieba.post.ui.slideback;

import android.graphics.Canvas;

public interface ISlideView {
    /**
     * 是否可以垂直滑动
     *
     * @return
     */
    boolean scrollVertical();

    /**
     * 宽度
     *
     * @return
     */
    int getWidth();

    /**
     * 高度
     *
     * @return
     */
    int getHeight();

    /**
     * 绘制
     *
     * @param canvas
     * @param currentWidth 根据手指滑动得出的当前宽度（最大值为getWidth())
     * @param orientation  滑动方向
     */
    void onDraw(Canvas canvas, float currentWidth, int orientation);
}
