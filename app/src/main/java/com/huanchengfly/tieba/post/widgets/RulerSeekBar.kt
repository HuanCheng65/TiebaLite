package com.huanchengfly.tieba.post.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.AbsSeekBar
import androidx.appcompat.widget.AppCompatSeekBar
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.dpToPxFloat

class RulerSeekBar : AppCompatSeekBar {
    /**
     * 刻度线画笔
     */
    private lateinit var mRulerPaint: Paint

    /**
     * 刻度线的个数,等分数等于刻度线的个数加1
     */
    private var mRulerCount: Int = 0

    /**
     * 每条刻度线的宽度
     */
    private var mRulerSize: Float = 0f

    /**
     * 刻度线的颜色
     */
    private var mRulerColor: Int = Color.WHITE

    /**
     * 滑块上面是否要显示刻度线
     */
    private var isShowTopOfThumb: Boolean = false

    /**
     * 边缘是否要显示刻度线
     */
    private var rulerOnEdge: Boolean = false

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        if (attrs != null) {
            val array = getContext().obtainStyledAttributes(
                attrs,
                R.styleable.RulerSeekBar,
                defStyleAttr,
                0
            )
            mRulerColor = array.getColor(R.styleable.RulerSeekBar_rulerColor, Color.WHITE)
            mRulerCount = array.getInteger(R.styleable.RulerSeekBar_rulerCount, 5)
            mRulerSize = array.getDimension(R.styleable.RulerSeekBar_rulerSize, 12f.dpToPxFloat())
            isShowTopOfThumb = array.getBoolean(R.styleable.RulerSeekBar_rulerShowTopOfThumb, false)
            rulerOnEdge = array.getBoolean(R.styleable.RulerSeekBar_rulerOnEdge, false)
            array.recycle()
        }
        init()
    }

    /**
     * 初始化
     */
    private fun init() {
        //创建绘制刻度线的画笔
        mRulerPaint = Paint()
        mRulerPaint.color = mRulerColor
        mRulerPaint.isAntiAlias = true

        //Api21及以上调用，去掉滑块后面的背景
        splitTrack = false
    }

    /**
     * 重写onDraw方法绘制刻度线
     *
     * @param canvas
     */
    @SuppressLint("DiscouragedPrivateApi")
    @Synchronized
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //极限条件校验
        if (width <= 0 || mRulerCount <= 0) {
            return
        }

        //计算刻度线的顶部坐标和底部坐标
        val rulerTop = (height / 2 - minimumHeight / 2) * 1F
        val rulerBottom = rulerTop + minimumHeight

        val rulerCenterY = (rulerTop + rulerBottom) / 2

        val rulerPadding = rulerCenterY - mRulerSize / 2 - rulerTop

        //获取每一份的长度
        val length = if (rulerOnEdge) {
            (width - paddingLeft - paddingRight - (mRulerCount + 2) * mRulerSize - rulerPadding * 2) / (mRulerCount + 1)
        } else {
            (width - paddingLeft - paddingRight - mRulerCount * mRulerSize) / (mRulerCount + 1)
        }
        if (rulerOnEdge) {
            canvas.drawCircle(
                paddingLeft + rulerPadding + mRulerSize / 2,
                rulerCenterY,
                mRulerSize / 2,
                mRulerPaint
            )
            canvas.drawCircle(
                paddingLeft + rulerPadding + (mRulerCount + 1) * (length + mRulerSize) + mRulerSize / 2,
                rulerCenterY,
                mRulerSize / 2,
                mRulerPaint
            )
        }
        //绘制刻度线
        for (i in 1..mRulerCount) {
            //计算刻度线的左边坐标和右边坐标
            val rulerLeft = if (rulerOnEdge) {
                (i * length + i * mRulerSize + paddingLeft + rulerPadding) * 1F
            } else {
                (i * length + (i - 1) * mRulerSize + paddingLeft) * 1F
            }
            val rulerRight = rulerLeft + mRulerSize

            val rulerCenterX = (rulerLeft + rulerRight) / 2

            //进行绘制
            canvas.drawCircle(rulerCenterX, rulerCenterY, mRulerSize / 2, mRulerPaint)
        }
        if (!isShowTopOfThumb) {
            try {
                val absSeekBarClazz = Class.forName("android.widget.AbsSeekBar")
                val absSeekBarDrawThumbMethod =
                    absSeekBarClazz.getDeclaredMethod("drawThumb", Canvas::class.java)
                absSeekBarDrawThumbMethod.isAccessible = true
                absSeekBarDrawThumbMethod.invoke(this as AbsSeekBar, canvas)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 设置刻度线的个数
     *
     * @param mRulerCount
     */
    fun setRulerCount(mRulerCount: Int) {
        this.mRulerCount = mRulerCount
        requestLayout()
    }

    /**
     * 设置刻度线的宽度，单位(px)
     *
     * @param mRulerWidth
     */
    fun setRulerWidth(mRulerWidth: Int) {
        this.mRulerSize = mRulerWidth.toFloat()
        requestLayout()
    }

    /**
     * 设置刻度线的颜色
     *
     * @param mRulerColor
     */
    fun setRulerColor(mRulerColor: Int) {
        this.mRulerColor = mRulerColor
        mRulerPaint.color = mRulerColor
        requestLayout()
    }

    /**
     * 滑块上面是否需要显示刻度线
     *
     * @param isShowTopOfThumb
     */
    fun setShowTopOfThumb(isShowTopOfThumb: Boolean) {
        this.isShowTopOfThumb = isShowTopOfThumb
        requestLayout()
    }
}