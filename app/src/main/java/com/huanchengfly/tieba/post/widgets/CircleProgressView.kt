package com.huanchengfly.tieba.post.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.getColorCompat
import java.util.*

open class CircleProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    var progress: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    private val progressBackgroundPaint: Paint
    private val progressPaint: Paint
    private val progressTextPaint: Paint

    private val progressWidth: Float
    var showProgressText: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    var progressBackgroundColor: Int
        get() = progressBackgroundPaint.color
        set(value) {
            progressBackgroundPaint.color = value
            invalidate()
        }
    var progressColor: Int
        get() = progressPaint.color
        set(value) {
            progressPaint.color = value
            invalidate()
        }
    var progressTextColor: Int
        get() = progressTextPaint.color
        set(value) {
            progressTextPaint.color = value
            invalidate()
        }
    var progressTextSize: Float
        get() = progressTextPaint.textSize
        set(value) {
            progressTextPaint.textSize = value
            invalidate()
        }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressView)

        progress = typedArray.getInt(
            R.styleable.CircleProgressView_progress,
            0
        )
        val progressBackgroundColor = typedArray.getColor(
            R.styleable.CircleProgressView_progressBackgroundColor,
            context.getColorCompat(R.color.default_color_unselected)
        )
        val progressColor = typedArray.getColor(
            R.styleable.CircleProgressView_progressColor,
            context.getColorCompat(R.color.default_color_primary)
        )
        val progressTextColor = typedArray.getColor(
            R.styleable.CircleProgressView_progressTextColor,
            context.getColorCompat(R.color.default_color_unselected)
        )
        showProgressText = typedArray.getBoolean(
            R.styleable.CircleProgressView_showProgressText,
            true
        )
        val progressTextSize = typedArray.getDimension(
            R.styleable.CircleProgressView_progressTextSize,
            12f
        )
        progressWidth = typedArray.getDimension(
            R.styleable.CircleProgressView_progressWidth,
            2f
        )

        progressBackgroundPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
            isDither = true
            strokeWidth = progressWidth
            color = progressBackgroundColor
        }

        progressPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
            isDither = true
            strokeWidth = progressWidth
            color = progressColor
        }

        progressTextPaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            isDither = true
            textSize = progressTextSize
            color = progressTextColor
        }

        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val viewWidth = width - paddingLeft - paddingRight
        val viewHeight = height - paddingTop - paddingBottom
        val size = if (viewWidth > viewHeight) viewHeight else viewWidth
        val rectLength = size - progressWidth

        val rectLeft = paddingLeft + (viewWidth - rectLength) / 2f
        val rectTop = paddingTop + (viewHeight - rectLength) / 2f
        val rectF = RectF(rectLeft, rectTop, rectLeft + rectLength, rectTop + rectLength)

        canvas.drawArc(rectF, 0F, 360F, false, progressBackgroundPaint)
        canvas.drawArc(rectF, 270F, 360F * (progress / 100F), false, progressPaint)

        if (showProgressText) {
            val progressText = String.format(Locale.getDefault(), "%d%%", progress)
            val fontMetrics = progressTextPaint.fontMetrics
            val textWidth = progressTextPaint.measureText(progressText)
            val textHeight = fontMetrics.bottom - fontMetrics.top
            val x = width / 2 - textWidth / 2
            val y = height / 2 + textHeight / 2 - fontMetrics.bottom
            canvas.drawText(progressText, x, y, progressTextPaint)
        }
    }
}