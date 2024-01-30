package com.huanchengfly.tieba.post.components.transformations

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.decode.internal.getOrCreate
import com.github.panpf.sketch.request.internal.RequestContext
import com.github.panpf.sketch.transform.TransformResult
import com.github.panpf.sketch.transform.Transformation
import com.huanchengfly.tieba.post.components.transformations.internal.FastBlur
import com.huanchengfly.tieba.post.components.transformations.internal.RSBlur
import com.huanchengfly.tieba.post.components.transformations.internal.SupportRSBlur

@Suppress("USELESS_ELVIS")
internal val Bitmap.safeConfig: Bitmap.Config
    get() = config ?: Bitmap.Config.ARGB_8888

class SketchBlurTransformation(
    val radius: Int = 25,
    private val sampling: Int = 1,
) : Transformation {
    override val key: String = "SketchBlurTransformation(radius=$radius, sampling=$sampling)"
    override suspend fun transform(
        sketch: Sketch,
        requestContext: RequestContext,
        input: Bitmap,
    ): TransformResult {
        val context = sketch.context

        val width: Int = input.width
        val height: Int = input.height
        val scaledWidth = width / sampling
        val scaledHeight = height / sampling

        var bitmap = sketch.bitmapPool.getOrCreate(
            scaledWidth,
            scaledHeight,
            input.safeConfig,
            requestContext.request.disallowReuseBitmap,
            "BlurTransformation"
        ).apply {
            density = input.density
        }

        val canvas = Canvas(bitmap)
        canvas.scale(1 / sampling.toFloat(), 1 / sampling.toFloat())
        val paint = Paint()
        paint.flags = Paint.FILTER_BITMAP_FLAG
        canvas.drawBitmap(input, 0f, 0f, paint)

        bitmap = try {
            SupportRSBlur.blur(context, bitmap, radius)
        } catch (e: NoClassDefFoundError) {
            RSBlur.blur(context, bitmap, radius)
        } catch (e: RuntimeException) {
            FastBlur.blur(bitmap, radius, true)
        }

        return TransformResult(bitmap, key)
    }
}