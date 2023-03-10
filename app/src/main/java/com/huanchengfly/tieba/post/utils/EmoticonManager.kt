package com.huanchengfly.tieba.post.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.panpf.sketch.request.LoadRequest
import com.github.panpf.sketch.request.LoadResult
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.fromJson
import com.huanchengfly.tieba.post.models.EmoticonCache
import com.huanchengfly.tieba.post.pxToDp
import com.huanchengfly.tieba.post.pxToSp
import com.huanchengfly.tieba.post.toJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.lang.ref.WeakReference

object EmoticonManager {
    private val DEFAULT_EMOTICON_MAPPING: Map<String, String> = mapOf(
        "呵呵" to "image_emoticon1",
        "哈哈" to "image_emoticon2",
        "吐舌" to "image_emoticon3",
        "啊" to "image_emoticon4",
        "酷" to "image_emoticon5",
        "怒" to "image_emoticon6",
        "开心" to "image_emoticon7",
        "汗" to "image_emoticon8",
        "泪" to "image_emoticon9",
        "黑线" to "image_emoticon10",
        "鄙视" to "image_emoticon11",
        "不高兴" to "image_emoticon12",
        "真棒" to "image_emoticon13",
        "钱" to "image_emoticon14",
        "疑问" to "image_emoticon15",
        "阴险" to "image_emoticon16",
        "吐" to "image_emoticon17",
        "咦" to "image_emoticon18",
        "委屈" to "image_emoticon19",
        "花心" to "image_emoticon20",
        "呼~" to "image_emoticon21",
        "笑眼" to "image_emoticon22",
        "冷" to "image_emoticon23",
        "太开心" to "image_emoticon24",
        "滑稽" to "image_emoticon25",
        "勉强" to "image_emoticon26",
        "狂汗" to "image_emoticon27",
        "乖" to "image_emoticon28",
        "睡觉" to "image_emoticon29",
        "惊哭" to "image_emoticon30",
        "生气" to "image_emoticon31",
        "惊讶" to "image_emoticon32",
        "喷" to "image_emoticon33",
        "爱心" to "image_emoticon34",
        "心碎" to "image_emoticon35",
        "玫瑰" to "image_emoticon36",
        "礼物" to "image_emoticon37",
        "彩虹" to "image_emoticon38",
        "星星月亮" to "image_emoticon39",
        "太阳" to "image_emoticon40",
        "钱币" to "image_emoticon41",
        "灯泡" to "image_emoticon42",
        "茶杯" to "image_emoticon43",
        "蛋糕" to "image_emoticon44",
        "音乐" to "image_emoticon45",
        "haha" to "image_emoticon46",
        "胜利" to "image_emoticon47",
        "大拇指" to "image_emoticon48",
        "弱" to "image_emoticon49",
        "OK" to "image_emoticon50",
        "生气" to "image_emoticon61",
        "沙发" to "image_emoticon77",
        "手纸" to "image_emoticon78",
        "香蕉" to "image_emoticon79",
        "便便" to "image_emoticon80",
        "药丸" to "image_emoticon81",
        "红领巾" to "image_emoticon82",
        "蜡烛" to "image_emoticon83",
        "三道杠" to "image_emoticon84",
    )

    private lateinit var contextRef: WeakReference<Context>
    private val emoticonIds: MutableList<String> = mutableListOf()
    private val emoticonMapping: MutableMap<String, String> = mutableMapOf()
    private val drawableCache: MutableMap<String, Drawable> = mutableMapOf()
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    @OptIn(ExperimentalTextApi::class)
    @Composable
    fun getEmoticonHeightPx(style: TextStyle): Int {
        val textMeasurer = rememberTextMeasurer()
        val textLayoutResult = textMeasurer.measure(
            AnnotatedString(stringResource(id = R.string.emoticon_default)),
            style
        )
        return textLayoutResult.size.height
    }

    fun getEmoticonInlineContent(
        sizePx: Int
    ): Map<String, InlineTextContent> {
        return emoticonIds.associate { id ->
            "Emoticon#$id" to InlineTextContent(
                placeholder = Placeholder(
                    sizePx.pxToSp().sp,
                    sizePx.pxToSp().sp,
                    PlaceholderVerticalAlign.TextCenter
                ),
                children = {
                    Image(
                        painter = rememberDrawablePainter(
                            drawable = getEmoticonDrawable(
                                LocalContext.current,
                                id
                            )
                        ),
                        contentDescription = stringResource(
                            id = R.string.emoticon,
                            getEmoticonNameById(id) ?: ""
                        ),
                        modifier = Modifier.size(sizePx.pxToDp().dp)
                    )
                }
            )
        }
    }

    fun init(context: Context) {
        contextRef = WeakReference(context)
        val emoticonCache = getEmoticonDataCache()
        if (emoticonCache.ids.isEmpty()) {
            for (i in 1..50) {
                emoticonIds.add("image_emoticon$i")
            }
            for (i in 61..101) {
                emoticonIds.add("image_emoticon$i")
            }
            for (i in 125..137) {
                emoticonIds.add("image_emoticon$i")
            }
        } else {
            emoticonIds.addAll(emoticonCache.ids)
        }
        if (emoticonCache.mapping.isEmpty()) {
            emoticonMapping.putAll(DEFAULT_EMOTICON_MAPPING)
        } else {
            emoticonMapping.putAll(emoticonCache.mapping)
        }
        updateCache()
        coroutineScope.launch {
            fetchEmoticons(context)
        }
    }

    private fun updateCache() {
        runCatching {
            val emoticonDataCacheFile = File(getEmoticonCacheDir(), "emoticon_data_cache")
            if (emoticonDataCacheFile.exists() || emoticonDataCacheFile.createNewFile()) {
                FileUtil.writeFile(
                    emoticonDataCacheFile,
                    EmoticonCache(emoticonIds, emoticonMapping).toJson(),
                    false
                )
            }
        }
    }

    private fun getContext(): Context {
        return contextRef.get() ?: App.INSTANCE
    }

    private fun getEmoticonDataCache(): EmoticonCache {
        return runCatching {
            File(getEmoticonCacheDir(), "emoticon_data_cache").takeIf { it.exists() }
                ?.fromJson<EmoticonCache>()
        }.getOrNull() ?: EmoticonCache()
    }

    private fun getEmoticonCacheDir(): File {
        return File(getContext().externalCacheDir ?: getContext().cacheDir, "emoticon").apply {
            if (exists() && isFile) {
                delete()
                mkdirs()
            } else if (!exists()) {
                mkdirs()
            }
        }
    }

    private fun getEmoticonFile(id: String): File {
        return File(getEmoticonCacheDir(), "$id.png")
    }

    fun getEmoticonIdByName(name: String): String? {
        return emoticonMapping[name]
    }

    private fun getEmoticonNameById(id: String): String? {
        return emoticonMapping.firstNotNullOfOrNull { (name, emoticonId) ->
            if (emoticonId == id) {
                name
            } else {
                null
            }
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun getEmoticonResId(context: Context, id: String): Int {
        return context.resources.getIdentifier(id, "drawable", context.packageName)
    }

    fun getEmoticonDrawable(context: Context, id: String?): Drawable? {
        if (id == null) {
            return null
        }
        if (drawableCache.containsKey(id)) {
            return drawableCache[id]
        }
        val resId = getEmoticonResId(context, id)
        if (resId != 0) {
            return AppCompatResources.getDrawable(context, resId).also { drawableCache[id] = it!! }
        }
        val emoticonFile = getEmoticonFile(id)
        if (!emoticonFile.exists()) {
            return null
        }
        return BitmapDrawable(
            getContext().resources,
            emoticonFile.inputStream()
        ).also { drawableCache[id] = it }
    }

    private suspend fun fetchEmoticons(context: Context) {
        emoticonIds.forEach {
            val resId = getEmoticonResId(context, it)
            val emoticonFile = getEmoticonFile(it)
            if (resId == 0 && !emoticonFile.exists()) {
                val loadEmoticonResult = LoadRequest(
                    context,
                    "http://static.tieba.baidu.com/tb/editor/images/client/$it.png"
                ).execute()
                if (loadEmoticonResult is LoadResult.Success) {
                    ImageUtil.bitmapToFile(
                        loadEmoticonResult.bitmap,
                        emoticonFile,
                        Bitmap.CompressFormat.PNG
                    )
                }
            }
        }
    }

    fun registerEmoticon(id: String, name: String) {
        val realId = if (id == "image_emoticon") "image_emoticon1" else id
        var changed = false
        if (!emoticonIds.contains(realId)) {
            emoticonIds.add(realId)
            changed = true
        }
        if (!emoticonMapping.containsKey(name)) {
            emoticonMapping[name] = realId
            changed = true
        }
        if (changed) {
            coroutineScope.launch {
                updateCache()
            }
        }
    }
}