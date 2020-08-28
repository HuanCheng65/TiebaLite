package com.huanchengfly.tieba.post.components

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.huanchengfly.tieba.post.R
import com.scwang.smart.refresh.layout.api.RefreshFooter
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.RefreshState
import com.scwang.smart.refresh.layout.constant.SpinnerStyle
import com.scwang.smart.refresh.layout.simple.SimpleComponent

class LoadMoreFooter @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SimpleComponent(context, attrs, defStyleAttr), RefreshFooter {
    protected var noMoreData = false

    @BindView(R.id.footer_progress)
    lateinit var progress: ProgressBar

    @BindView(R.id.footer_text)
    lateinit var textView: TextView

    override fun onStartAnimator(refreshLayout: RefreshLayout, height: Int, maxDragHeight: Int) {
        super.onStartAnimator(refreshLayout, height, maxDragHeight)
        progress.visibility = View.VISIBLE
    }

    override fun onFinish(refreshLayout: RefreshLayout, success: Boolean): Int {
        progress.visibility = View.GONE
        if (noMoreData) {
            textView.setText(R.string.tip_load_end)
        } else {
            if (success) {
                textView.setText(R.string.text_load_success)
            } else {
                textView.setText(R.string.text_load_failed)
            }
        }
        return 0
    }

    override fun isSupportHorizontalDrag(): Boolean {
        return false
    }

    override fun onStateChanged(refreshLayout: RefreshLayout, oldState: RefreshState, newState: RefreshState) {
        if (!noMoreData) {
            when (newState) {
                RefreshState.None,
                RefreshState.PullUpToLoad -> {
                    progress.visibility = View.GONE
                    textView.setText(R.string.text_pull_up_to_load)
                }
                RefreshState.Loading -> {
                    progress.visibility = View.VISIBLE
                    textView.setText(R.string.text_loading)
                }
                RefreshState.ReleaseToLoad -> {
                    progress.visibility = View.GONE
                    textView.setText(R.string.text_release_to_load)
                }
                else -> {
                    progress.visibility = View.GONE
                    textView.text = null
                }
            }
        }
    }

    override fun setNoMoreData(noMoreData: Boolean): Boolean {
        if (this.noMoreData != noMoreData) {
            this.noMoreData = noMoreData
            if (noMoreData) {
                textView.setText(R.string.tip_load_end)
            } else {
                textView.setText(R.string.text_pull_up_to_load)
            }
        }
        return true
    }

    init {
        gravity = Gravity.CENTER
        View.inflate(context, R.layout.footer_load_more, this)
        ButterKnife.bind(this)
    }

    override fun getView(): View {
        return this
    }

    override fun getSpinnerStyle(): SpinnerStyle {
        return SpinnerStyle.Translate
    }
}