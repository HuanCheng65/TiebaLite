package com.huanchengfly.tieba.post.components

import android.view.LayoutInflater
import android.view.View
import com.github.piasy.biv.indicator.ProgressIndicator
import com.github.piasy.biv.view.BigImageView
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.widgets.CircleProgressView

class CircleProgressIndicator : ProgressIndicator {
    private var circleProgressView: CircleProgressView? = null

    override fun getView(parent: BigImageView): View {
        circleProgressView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_circle_progress, parent, false) as CircleProgressView
        return circleProgressView!!
    }

    override fun onStart() {}

    override fun onProgress(progress: Int) {
        if (progress < 0 || progress > 100 || circleProgressView == null) {
            return
        }
        circleProgressView!!.progress = progress
    }

    override fun onFinish() {}
}