package com.huanchengfly.tieba.post.activities

import android.os.Bundle
import android.util.TypedValue
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.huanchengfly.tieba.post.*
import com.huanchengfly.tieba.post.adapters.ChatBubbleStyleAdapter
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.widgets.RulerSeekBar


class AppFontSizeActivity : BaseActivity() {
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.collapsing_toolbar)
    lateinit var collapsingToolbar: CollapsingToolbarLayout

    @BindView(R.id.app_font_size_seekbar)
    lateinit var seekBar: RulerSeekBar

    @BindView(R.id.app_font_size_text)
    lateinit var sizeText: TextView

    @BindView(R.id.app_font_size_bubbles)
    lateinit var chatBubblesRv: RecyclerView

    var oldFontSize: Float = 0f
    var finished: Boolean = false

    private val bubblesAdapter: ChatBubbleStyleAdapter by lazy {
        ChatBubbleStyleAdapter(
            this,
            listOf(
                ChatBubbleStyleAdapter.Bubble(
                    getString(R.string.bubble_want_change_font_size),
                    ChatBubbleStyleAdapter.Bubble.POSITION_RIGHT
                ),
                ChatBubbleStyleAdapter.Bubble(getString(R.string.bubble_change_font_size))
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background))
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = this@AppFontSizeActivity.title
        }
        collapsingToolbar.title = title
        oldFontSize = appPreferences.fontScale
        chatBubblesRv.apply {
            layoutManager =
                MyLinearLayoutManager(this@AppFontSizeActivity, LinearLayoutManager.VERTICAL, false)
            adapter = bubblesAdapter
        }
        val progress =
            ((appPreferences.fontScale * 1000L - FONT_SCALE_MIN * 1000L).toInt()) / ((FONT_SCALE_STEP * 1000L).toInt())
        seekBar.progress = progress
        updateSizeText(progress)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val fontScale = FONT_SCALE_MIN + progress * FONT_SCALE_STEP
                appPreferences.fontScale = fontScale
                updatePreview(fontScale)
                updateSizeText(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun finish() {
        if (!finished && oldFontSize != appPreferences.fontScale) {
            finished = true
            toastShort(R.string.toast_after_change_will_restart)
            BaseApplication.instance.removeAllActivity()
            goToActivity<MainActivity>()
        }
        super.finish()
    }

    fun updateSizeText(progress: Int) {
        val sizeTexts = SIZE_TEXT_MAPPING.filterValues {
            progress in it
        }
        if (sizeTexts.isNotEmpty()) {
            sizeText.setText(sizeTexts.map { it.key }[0])
        }
    }

    fun updatePreview(fontScale: Float = appPreferences.fontScale) {
        bubblesAdapter.bubblesFontSize = 15f.dpToPxFloat() * fontScale
        sizeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16f.dpToPxFloat() * fontScale)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_app_font_size
    }

    companion object {
        const val FONT_SCALE_MIN = 0.8f
        const val FONT_SCALE_MAX = 1.3f
        const val FONT_SCALE_STEP = 0.05f
        const val DEFAULT_FONT_SCALE = 1f

        val SIZE_TEXT_MAPPING = mapOf(
            R.string.text_size_small to 0..1,
            R.string.text_size_little_small to 2..3,
            R.string.text_size_default to 4..4,
            R.string.text_size_little_large to 5..6,
            R.string.text_size_large to 7..8,
            R.string.text_size_very_large to 9..10
        )
    }
}