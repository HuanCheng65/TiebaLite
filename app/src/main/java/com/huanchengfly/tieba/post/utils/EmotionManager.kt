package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.huanchengfly.tieba.post.BaseApplication
import com.huanchengfly.tieba.post.models.EmotionCache
import com.huanchengfly.tieba.post.toJson
import java.io.File
import java.lang.ref.WeakReference
import java.util.concurrent.Executors

object EmotionManager {
    private val DEFAULT_EMOTION_MAPPING: Map<String, String> = mapOf(
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
    private val emotionIds: MutableList<String> = mutableListOf()
    private val emotionMapping: MutableMap<String, String> = mutableMapOf()
    private val drawableCache: MutableMap<String, Drawable> = mutableMapOf()

    private val executor by lazy { Executors.newCachedThreadPool() }

    fun init(context: Context) {
        contextRef = WeakReference(context)
        val emotionCache = getEmotionDataCache()
        if (emotionCache.ids.isEmpty()) {
            for (i in 1..137) {
                emotionIds.add("image_emoticon$i")
            }
        } else {
            emotionIds.addAll(emotionCache.ids)
        }
        if (emotionCache.mapping.isEmpty()) {
            emotionMapping.putAll(DEFAULT_EMOTION_MAPPING)
        } else {
            emotionMapping.putAll(emotionCache.mapping)
        }
        updateCache()
        executor.submit {
            fetchEmotions()
        }
    }

    private fun updateCache() {
        val emotionDataCacheFile = File(getEmotionCacheDir(), "emotion_data_cache")
        if (!emotionDataCacheFile.exists()) {
            emotionDataCacheFile.createNewFile()
        }
        FileUtil.writeFile(
            emotionDataCacheFile,
            EmotionCache(emotionIds, emotionMapping).toJson(),
            false
        )
    }

    private fun getContext(): Context {
        return contextRef.get() ?: BaseApplication.instance
    }

    fun getEmotionDataCache(): EmotionCache {
        val emotionDataCacheFile = File(getEmotionCacheDir(), "emotion_data_cache")
        if (emotionDataCacheFile.exists()) {
            return GsonUtil.getGson()
                .fromJson(emotionDataCacheFile.reader(), EmotionCache::class.java)
        }
        return EmotionCache()
    }

    fun getEmotionCacheDir(): File {
        return File(getContext().externalCacheDir ?: getContext().cacheDir, "emotion")
    }

    fun getEmotionFile(id: String): File {
        val emotionsCacheDir = getEmotionCacheDir()
        if (emotionsCacheDir.exists() && emotionsCacheDir.isFile) {
            emotionsCacheDir.delete()
            emotionsCacheDir.mkdirs()
        } else if (!emotionsCacheDir.exists()) {
            emotionsCacheDir.mkdirs()
        }
        return File(emotionsCacheDir, "$id.png")
    }

    fun getEmotionIdByName(name: String): String? {
        return emotionMapping[name]
    }

    fun getEmotionDrawable(id: String?): Drawable? {
        if (id == null) {
            return null
        }
        if (drawableCache.containsKey(id)) {
            return drawableCache[id]
        }
        val emotionFile = getEmotionFile(id)
        if (!emotionFile.exists()) {
            return null
        }
        return BitmapDrawable(
            getContext().resources,
            emotionFile.inputStream()
        ).also { drawableCache[id] = it }
    }

    private fun fetchEmotions() {
        emotionIds.forEach {
            val emotionFile = getEmotionFile(it)
            if (!emotionFile.exists()) {
                try {
                    val emotionBitmap =
                        Glide.with(getContext())
                            .asBitmap()
                            .load("http://static.tieba.baidu.com/tb/editor/images/client/$it.png")
                            .submit()
                            .get()
                    ImageUtil.bitmapToFile(
                        emotionBitmap,
                        emotionFile,
                        Bitmap.CompressFormat.PNG
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun registerEmotion(id: String, name: String) {
        val realId = if (id == "image_emoticon") "image_emoticon1" else id
        var changed = false
        if (!emotionIds.contains(realId)) {
            emotionIds.add(realId)
            changed = true
        }
        if (!emotionMapping.containsKey(name)) {
            emotionMapping[name] = realId
            changed = true
        }
        if (changed) updateCache()
    }
}