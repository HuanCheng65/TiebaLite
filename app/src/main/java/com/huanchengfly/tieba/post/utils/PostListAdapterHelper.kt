package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.graphics.Bitmap
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.huanchengfly.tieba.post.BaseApplication
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.PhotoViewActivity.Companion.OBJ_TYPE_THREAD_PAGE
import com.huanchengfly.tieba.post.activities.WebViewActivity
import com.huanchengfly.tieba.post.api.models.ThreadContentBean
import com.huanchengfly.tieba.post.api.models.ThreadContentBean.PostListItemBean
import com.huanchengfly.tieba.post.components.LinkTouchMovementMethod
import com.huanchengfly.tieba.post.components.spans.MyImageSpan
import com.huanchengfly.tieba.post.components.spans.MyURLSpan
import com.huanchengfly.tieba.post.components.spans.MyUserSpan
import com.huanchengfly.tieba.post.dpToPx
import com.huanchengfly.tieba.post.models.PhotoViewBean
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.BilibiliUtil.replaceVideoNumberSpan
import com.huanchengfly.tieba.post.widgets.MyImageView
import com.huanchengfly.tieba.post.widgets.VideoPlayerStandard
import com.huanchengfly.tieba.post.widgets.VoicePlayerView
import com.huanchengfly.tieba.post.widgets.theme.TintMySpannableTextView
import java.util.*
import kotlin.collections.ArrayList

class PostListAdapterHelper(
    private val context: Context
) {
    var seeLz: Boolean = false
    var pureRead: Boolean = false
    private var dataBean: ThreadContentBean? = null
    private var photoViewBeansMap: TreeMap<Int, List<PhotoViewBean>> = TreeMap()

    private val defaultLayoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

    init {
        defaultLayoutParams.setMargins(0, 8, 0, 8)
    }

    fun setData(data: ThreadContentBean) {
        dataBean = data
        setPic(data.postList)
    }

    fun addData(data: ThreadContentBean) {
        dataBean = data
        addPic(data.postList)
    }

    private fun setPic(postListItemBeans: List<PostListItemBean>?) {
        photoViewBeansMap = TreeMap()
        addPic(postListItemBeans)
    }

    private fun addPic(postListItemBeans: List<PostListItemBean>?) {
        if (postListItemBeans != null) {
            for (postListItemBean in postListItemBeans) {
                val photoViewBeans: MutableList<PhotoViewBean> = ArrayList()
                if (postListItemBean.content.isNullOrEmpty() || postListItemBean.floor == null) {
                    continue
                }
                for (contentBean in postListItemBean.content) {
                    if (contentBean.type == "3") {
                        if (contentBean.originSrc == null) {
                            continue
                        }
                        val url = ImageUtil.getUrl(
                            context,
                            true,
                            contentBean.originSrc,
                            contentBean.bigCdnSrc,
                            contentBean.cdnSrcActive,
                            contentBean.cdnSrc
                        )
                        if (url.isNullOrEmpty()) {
                            continue
                        }
                        photoViewBeans.add(
                            PhotoViewBean(
                                url,
                                ImageUtil.getNonNullString(
                                    contentBean.originSrc,
                                    contentBean.bigCdnSrc,
                                    contentBean.cdnSrcActive,
                                    contentBean.cdnSrc
                                ),
                                "1" == contentBean.isLongPic
                            )
                        )
                    }
                }
                photoViewBeansMap[Integer.valueOf(postListItemBean.floor)] = photoViewBeans
            }
        }
    }

    private fun createTextView(): TextView {
        val textView: TextView
        val mySpannableTextView = TintMySpannableTextView(context)
        mySpannableTextView.setTintResId(R.color.default_color_text)
        mySpannableTextView.setLinkTouchMovementMethod(LinkTouchMovementMethod.getInstance())
        textView = mySpannableTextView
        textView.setFocusable(false)
        textView.setClickable(false)
        textView.setLongClickable(false)
        textView.setTextIsSelectable(false)
        textView.setOnClickListener(null)
        textView.setOnLongClickListener(null)
        textView.setLetterSpacing(0.02f)
        textView.setTextSize(16f)
        if (pureRead) {
            textView.setLineSpacing(0.5f, 1.3f)
        } else {
            textView.setLineSpacing(0.5f, 1.2f)
        }
        return textView
    }

    private fun setText(textView: TextView, content: CharSequence?) {
        var text = content
        text = replaceVideoNumberSpan(context, text)
        text = StringUtil.getEmotionContent(EmotionUtil.EMOTION_ALL_TYPE, textView, text)
        textView.text = text
    }

    private fun getMaxWidth(floor: String): Float {
        var maxWidth: Float =
            BaseApplication.ScreenInfo.EXACT_SCREEN_WIDTH.toFloat() - (24 * 2 + 38).dpToPx()
        if (pureRead || "1" == floor) {
            maxWidth =
                BaseApplication.ScreenInfo.EXACT_SCREEN_WIDTH.toFloat() - (16 * 2 + 4).dpToPx()
        }
        return maxWidth
    }

    private fun getLayoutParams(
        contentBean: ThreadContentBean.ContentBean,
        floor: String
    ): LinearLayout.LayoutParams {
        if ("3" != contentBean.type && "20" != contentBean.type && "5" != contentBean.type) {
            return defaultLayoutParams
        }
        var widthFloat: Float
        var heightFloat: Float
        if (contentBean.type == "3" || contentBean.type == "20") {
            val strings = contentBean.bsize!!.split(",".toRegex()).toTypedArray()
            widthFloat = java.lang.Float.valueOf(strings[0])
            heightFloat = java.lang.Float.valueOf(strings[1])
            heightFloat *= getMaxWidth(floor) / widthFloat
            widthFloat = getMaxWidth(floor)
        } else {
            val width = java.lang.Float.valueOf(contentBean.width!!)
            widthFloat = getMaxWidth(floor)
            heightFloat = java.lang.Float.valueOf(contentBean.height!!)
            heightFloat *= widthFloat / width
        }
        val width = Math.round(widthFloat)
        val height = Math.round(heightFloat)
        val layoutParams = LinearLayout.LayoutParams(width, height)
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL
        val dp16 = DisplayUtil.dp2px(context, 16f)
        val dp4 = DisplayUtil.dp2px(context, 4f)
        val dp2 = DisplayUtil.dp2px(context, 2f)
        if ("1" == floor) {
            layoutParams.setMargins(dp16, dp2, dp16, dp2)
        } else {
            layoutParams.setMargins(dp4, dp2, dp4, dp2)
        }
        return layoutParams
    }

    private fun appendTextToLastTextView(views: List<View>, newContent: CharSequence?): Boolean {
        val lastView = views.lastOrNull()
        if (lastView is TextView) {
            val spannableStringBuilder = SpannableStringBuilder(lastView.text)
            spannableStringBuilder.append(newContent ?: "")
            setText(lastView, spannableStringBuilder)
            return false
        }
        return true
    }

    private fun getLinkContent(newContent: CharSequence?, url: String): CharSequence {
        return getLinkContent("", newContent, url)
    }

    private fun getLinkContent(
        oldContent: CharSequence,
        newContent: CharSequence?,
        url: String
    ): CharSequence {
        val linkIconText = "[链接]"
        val s = " "
        val start = oldContent.length
        val end = start + s.length + linkIconText.length + (newContent ?: "").length
        val spannableStringBuilder = SpannableStringBuilder(oldContent)
        var bitmap = Util.getBitmapFromVectorDrawable(context, R.drawable.ic_link)
        val size = DisplayUtil.sp2px(context, 16f)
        val color: Int = ThemeUtils.getColorByAttr(context, R.attr.colorAccent)
        bitmap = Bitmap.createScaledBitmap(bitmap!!, size, size, true)
        bitmap = Util.tintBitmap(bitmap, color)
        spannableStringBuilder.append(
            linkIconText,
            MyImageSpan(context, bitmap),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableStringBuilder.append(s)
        spannableStringBuilder.append(newContent)
        spannableStringBuilder.setSpan(
            MyURLSpan(context, url),
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannableStringBuilder
    }

    private fun appendLinkToLastTextView(
        views: List<View>,
        newContent: CharSequence?,
        url: String
    ): Boolean {
        val lastView = views.lastOrNull()
        if (lastView is TextView) {
            setText(lastView, getLinkContent(lastView.text, newContent, url))
            return false
        }
        return true
    }

    private fun getUserContent(newContent: CharSequence?, uid: String): CharSequence {
        return getUserContent("", newContent, uid)
    }

    private fun getUserContent(
        oldContent: CharSequence,
        newContent: CharSequence?,
        uid: String
    ): CharSequence {
        val start = oldContent.length
        val end = start + (newContent ?: "").length
        val spannableStringBuilder = SpannableStringBuilder(oldContent)
        spannableStringBuilder.append(newContent)
        spannableStringBuilder.setSpan(
            MyUserSpan(context, uid),
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannableStringBuilder
    }

    private fun appendUserToLastTextView(
        views: List<View>,
        newContent: CharSequence?,
        uid: String
    ): Boolean {
        val lastView = views.lastOrNull()
        if (lastView is TextView) {
            setText(lastView, getUserContent(lastView.text, newContent, uid))
            return false
        }
        return true
    }

    private fun getPhotoViewBeans(): List<PhotoViewBean> {
        val photoViewBeans: MutableList<PhotoViewBean> = mutableListOf()
        for (key in photoViewBeansMap.keys) {
            if (photoViewBeansMap.get(key) != null) photoViewBeans.addAll(
                photoViewBeansMap[key]
                    ?: emptyList()
            )
        }
        return photoViewBeans
    }

    fun getContentViews(postListItemBean: PostListItemBean): List<View> {
        val views: MutableList<View> = ArrayList()
        for (contentBean in postListItemBean.content!!) {
            when (contentBean.type) {
                "0", "9" -> {
                    if (appendTextToLastTextView(views, contentBean.text)) {
                        val textView: TextView = createTextView()
                        textView.layoutParams =
                            getLayoutParams(contentBean, postListItemBean.floor!!)
                        setText(textView, contentBean.text)
                        views.add(textView)
                    }
                }
                "1" -> if (appendLinkToLastTextView(views, contentBean.text, contentBean.link!!)) {
                    val textView: TextView = createTextView()
                    textView.layoutParams = getLayoutParams(contentBean, postListItemBean.floor!!)
                    setText(textView, getLinkContent(contentBean.text, contentBean.link))
                    views.add(textView)
                }
                "2" -> {
                    val emojiText = "#(" + contentBean.c + ")"
                    if (appendTextToLastTextView(views, emojiText)) {
                        val textView: TextView = createTextView()
                        textView.layoutParams =
                            getLayoutParams(contentBean, postListItemBean.floor!!)
                        setText(textView, emojiText)
                        views.add(textView)
                    }
                }
                "3" -> {
                    val url = ImageUtil.getUrl(
                        context,
                        true,
                        contentBean.originSrc!!,
                        contentBean.bigCdnSrc,
                        contentBean.cdnSrcActive,
                        contentBean.cdnSrc
                    )
                    if (TextUtils.isEmpty(url)) {
                        break
                    }
                    val imageView = MyImageView(context)
                    imageView.layoutParams = getLayoutParams(contentBean, postListItemBean.floor!!)
                    imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                    ImageUtil.load(imageView, ImageUtil.LOAD_TYPE_SMALL_PIC, url)
                    val photoViewBeans: List<PhotoViewBean> = getPhotoViewBeans()
                    for (photoViewBean in photoViewBeans) {
                        if (TextUtils.equals(photoViewBean.originUrl, contentBean.originSrc)) {
                            ImageUtil.initImageView(
                                imageView,
                                photoViewBeans,
                                photoViewBeans.indexOf(photoViewBean),
                                dataBean!!.forum!!.name,
                                dataBean!!.forum!!.id,
                                dataBean!!.thread!!.id,
                                seeLz,
                                OBJ_TYPE_THREAD_PAGE
                            )
                            break
                        }
                    }
                    views.add(imageView)
                }
                "4" -> if (appendUserToLastTextView(views, contentBean.text, contentBean.uid!!)) {
                    val textView: TextView = createTextView()
                    textView.layoutParams = getLayoutParams(contentBean, postListItemBean.floor!!)
                    setText(textView, getUserContent(contentBean.text, contentBean.uid))
                    views.add(textView)
                }
                "5" -> if (contentBean.src != null && contentBean.width != null && contentBean.height != null) {
                    if (contentBean.link != null) {
                        val videoPlayerStandard = VideoPlayerStandard(context)
                        videoPlayerStandard.setUp(contentBean.link, "")
                        videoPlayerStandard.layoutParams =
                            getLayoutParams(contentBean, postListItemBean.floor!!)
                        videoPlayerStandard.id = R.id.video_player
                        ImageUtil.load(
                            videoPlayerStandard.posterImageView,
                            ImageUtil.LOAD_TYPE_SMALL_PIC,
                            contentBean.src,
                            true
                        )
                        views.add(videoPlayerStandard)
                    } else {
                        val videoImageView = MyImageView(context)
                        videoImageView.layoutParams =
                            getLayoutParams(contentBean, postListItemBean.floor!!)
                        videoImageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                        ImageUtil.load(
                            videoImageView,
                            ImageUtil.LOAD_TYPE_SMALL_PIC,
                            contentBean.src,
                            true
                        )
                        videoImageView.setOnClickListener {
                            WebViewActivity.launch(context, contentBean.text)
                        }
                        views.add(videoImageView)
                    }
                } else {
                    if (appendLinkToLastTextView(
                            views,
                            "[视频] " + contentBean.text,
                            contentBean.text!!
                        )
                    ) {
                        val textView: TextView = createTextView()
                        textView.layoutParams = defaultLayoutParams
                        setText(
                            textView,
                            getLinkContent("[视频] " + contentBean.text, contentBean.text!!)
                        )
                        views.add(textView)
                    }
                }
                "10" -> {
                    val voiceUrl =
                        "http://c.tieba.baidu.com/c/p/voice?voice_md5=" + contentBean.voiceMD5 + "&play_from=pb_voice_play"
                    val voicePlayerView = VoicePlayerView(context)
                    voicePlayerView.layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    voicePlayerView.duration = Integer.valueOf(contentBean.duringTime!!)
                    voicePlayerView.url = voiceUrl
                    views.add(voicePlayerView)
                }
                "20" -> {
                    val memeImageView = MyImageView(context)
                    memeImageView.layoutParams =
                        getLayoutParams(contentBean, postListItemBean.floor!!)
                    memeImageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                    ImageUtil.load(memeImageView, ImageUtil.LOAD_TYPE_SMALL_PIC, contentBean.src)
                    ImageUtil.initImageView(
                        memeImageView,
                        PhotoViewBean(contentBean.src, contentBean.src, false)
                    )
                    views.add(memeImageView)
                }
                else -> {
                }
            }
        }
        return views
    }

}