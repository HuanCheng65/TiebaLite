package com.huanchengfly.tieba.post.plugins.asoulcnki

import android.graphics.Bitmap
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.ThreadContentBean
import com.huanchengfly.tieba.post.components.LinkTouchMovementMethod
import com.huanchengfly.tieba.post.components.spans.MyImageSpan
import com.huanchengfly.tieba.post.plugins.IPlugin
import com.huanchengfly.tieba.post.plugins.asoulcnki.api.CheckApi
import com.huanchengfly.tieba.post.plugins.asoulcnki.models.CheckApiBody
import com.huanchengfly.tieba.post.plugins.interfaces.IApp
import com.huanchengfly.tieba.post.plugins.models.PluginManifest
import com.huanchengfly.tieba.post.plugins.registerMenuItem
import com.huanchengfly.tieba.post.toJson
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.DisplayUtil
import com.huanchengfly.tieba.post.utils.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PluginAsoulCnki(app: IApp, manifest: PluginManifest) : IPlugin(app, manifest) {
    override fun onEnable() {
        super.onEnable()
        registerMenuItem<ThreadContentBean.PostListItemBean>(
            "asoul_cnki_check",
            context.getString(R.string.plugin_asoul_cnki_check)
        ) { data ->
            val dialog = app.showLoadingDialog()
            val body = CheckApiBody(getPostTextContent(data)).toJson()
            launch(Dispatchers.IO + job) {
                val result =
                    CheckApi.instance.checkAsync(body.toRequestBody("application/json, charset=utf-8".toMediaTypeOrNull()))
                        .await()
                launch(Dispatchers.Main + job) {
                    dialog.cancel()
                    if (result.code == 0) {
                        val numberFormatter = NumberFormat.getNumberInstance().apply {
                            maximumFractionDigits = 2
                            minimumFractionDigits = 2
                        }
                        app.showAlertDialog {
                            setTitle("查重结果")
                            val percent = "${numberFormatter.format(result.data.rate * 100.0)}%"
                            val resultForCopy = context.getString(
                                R.string.plugin_asoul_cnki_result,
                                formatDateTime("yyyy-MM-dd HH:mm:ss"),
                                percent,
                                if (result.data.related.isNotEmpty()) {
                                    context.getString(
                                        R.string.plugin_asoul_cnki_related,
                                        result.data.related[0].replyUrl,
                                        result.data.related[0].reply.mName,
                                        formatDateTime(
                                            "yyyy-MM-dd HH:mm",
                                            result.data.related[0].reply.ctime * 1000L
                                        )
                                    )
                                } else {
                                    ""
                                }
                            )
                            val view = View.inflate(
                                context,
                                R.layout.plugin_asoul_cnki_dialog_check_result,
                                null
                            )
                            val percentView = view.findViewById<TextView>(R.id.check_result_percent)
                            val progress =
                                view.findViewById<ProgressBar>(R.id.check_result_progress)
                            val relatedView = view.findViewById<View>(R.id.check_result_related)
                            val relatedTitle =
                                view.findViewById<TextView>(R.id.check_result_related_title)
                            val relatedContent =
                                view.findViewById<TextView>(R.id.check_result_related_content)
                            percentView.text = context.getString(
                                R.string.plugin_asoul_cnki_check_result_percent,
                                percent
                            )
                            progress.progress = (result.data.rate * 10000).toInt()
                            if (result.data.related.isNullOrEmpty()) {
                                relatedView.visibility = View.GONE
                            } else {
                                relatedView.visibility = View.VISIBLE
                                relatedTitle.text = context.getString(
                                    R.string.plugin_asoul_cnki_check_result_related,
                                    result.data.related.size
                                )
                            }
                            val relatedContentText = SpannableStringBuilder()
                            result.data.related.forEach {
                                relatedContentText.appendLink("${it.reply.mName} 的评论", it.replyUrl)
                                    .append("\n")
                            }
                            relatedContent.apply {
                                text = relatedContentText
                                movementMethod = LinkTouchMovementMethod.getInstance()
                            }
                            setView(view)
                            setPositiveButton(R.string.btn_copy_check_result) { _, _ ->
                                app.copyText(resultForCopy)
                            }
                            setNegativeButton(R.string.btn_close, null)
                        }
                    } else {
                        app.toastShort("查重失败 ${result.code}")
                    }
                }
            }
        }
    }

    private fun SpannableStringBuilder.appendLink(
        text: CharSequence,
        url: String
    ): SpannableStringBuilder {
        val spannableStringBuilder = SpannableStringBuilder()
        val size = DisplayUtil.sp2px(context, 14f)
        val bitmap = Util.tintBitmap(
            Bitmap.createScaledBitmap(
                Util.getBitmapFromVectorDrawable(
                    context,
                    R.drawable.ic_link
                ),
                size,
                size,
                true
            ),
            ThemeUtils.getColorByAttr(context, R.attr.colorAccent)
        )
        spannableStringBuilder.append(
            "[链接]",
            MyImageSpan(context, bitmap),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableStringBuilder.append(" ")
        spannableStringBuilder.append(
            text,
            MyURLSpan(context, url),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return append(spannableStringBuilder)
    }

    fun getPostTextContent(item: ThreadContentBean.PostListItemBean): String {
        val stringBuilder = StringBuilder()
        for (contentBean in item.content!!) {
            when (contentBean.type) {
                "2" -> contentBean.setText("#(" + contentBean.c + ")")
                "3", "20" -> contentBean.setText("[图片]\n")
                "10" -> contentBean.setText("[语音]\n")
            }
            if (contentBean.text != null) {
                stringBuilder.append(contentBean.text)
            }
        }
        return stringBuilder.toString()
    }

    private fun formatDateTime(
        pattern: String,
        timestamp: Long = System.currentTimeMillis()
    ): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
    }
}