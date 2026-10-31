/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Forked from accompanist-placeholder and accompanist-placeholder-material 0.34.0, which are
// deprecated upstream. The foundation and Material variants are merged into a single modifier.

package com.huanchengfly.tieba.post.ui.widgets.compose.placeholder

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.LayoutDirection

object PlaceholderDefaults {
    val fadeAnimationSpec: InfiniteRepeatableSpec<Float> by lazy {
        infiniteRepeatable(
            animation = tween(delayMillis = 200, durationMillis = 600),
            repeatMode = RepeatMode.Reverse,
        )
    }

    @Composable
    fun color(
        backgroundColor: Color = MaterialTheme.colors.surface,
        contentColor: Color = contentColorFor(backgroundColor),
        contentAlpha: Float = 0.1f,
    ): Color = contentColor.copy(contentAlpha).compositeOver(backgroundColor)

    @Composable
    fun fadeHighlightColor(
        backgroundColor: Color = MaterialTheme.colors.surface,
        alpha: Float = 0.3f,
    ): Color = backgroundColor.copy(alpha = alpha)
}

/**
 * Provides a brush to paint the placeholder based on the highlight animation progress.
 */
@Stable
interface PlaceholderHighlight {
    val animationSpec: InfiniteRepeatableSpec<Float>?

    fun brush(progress: Float, size: Size): Brush

    fun alpha(progress: Float): Float

    companion object
}

fun PlaceholderHighlight.Companion.fade(
    highlightColor: Color,
    animationSpec: InfiniteRepeatableSpec<Float> = PlaceholderDefaults.fadeAnimationSpec,
): PlaceholderHighlight = Fade(highlightColor, animationSpec)

@Composable
fun PlaceholderHighlight.Companion.fade(
    animationSpec: InfiniteRepeatableSpec<Float> = PlaceholderDefaults.fadeAnimationSpec,
): PlaceholderHighlight = fade(PlaceholderDefaults.fadeHighlightColor(), animationSpec)

private data class Fade(
    private val highlightColor: Color,
    override val animationSpec: InfiniteRepeatableSpec<Float>,
) : PlaceholderHighlight {
    private val brush = SolidColor(highlightColor)

    override fun brush(progress: Float, size: Size): Brush = brush
    override fun alpha(progress: Float): Float = progress
}

/**
 * Draws skeleton UI over the content while it is loading, cross-fading between the two when
 * [visible] changes.
 *
 * @param color placeholder color, defaults to [PlaceholderDefaults.color].
 * @param shape placeholder shape, defaults to the small shape of [MaterialTheme.shapes].
 */
fun Modifier.placeholder(
    visible: Boolean,
    color: Color = Color.Unspecified,
    shape: Shape? = null,
    highlight: PlaceholderHighlight? = null,
    placeholderFadeTransitionSpec: @Composable Transition.Segment<Boolean>.() -> FiniteAnimationSpec<Float> = { spring() },
    contentFadeTransitionSpec: @Composable Transition.Segment<Boolean>.() -> FiniteAnimationSpec<Float> = { spring() },
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "placeholder"
        value = visible
        properties["visible"] = visible
        properties["color"] = color
        properties["highlight"] = highlight
        properties["shape"] = shape
    }
) {
    val placeholderColor = if (color.isSpecified) color else PlaceholderDefaults.color()
    val placeholderShape = shape ?: MaterialTheme.shapes.small

    val cache = remember { OutlineCache() }

    var highlightProgress by remember { mutableFloatStateOf(0f) }

    val transitionState = remember { MutableTransitionState(visible) }.apply {
        targetState = visible
    }
    val transition = updateTransition(transitionState, "placeholder_crossfade")

    val placeholderAlpha by transition.animateFloat(
        transitionSpec = placeholderFadeTransitionSpec,
        label = "placeholder_fade",
        targetValueByState = { placeholderVisible -> if (placeholderVisible) 1f else 0f }
    )
    val contentAlpha by transition.animateFloat(
        transitionSpec = contentFadeTransitionSpec,
        label = "content_fade",
        targetValueByState = { placeholderVisible -> if (placeholderVisible) 0f else 1f }
    )

    val animationSpec = highlight?.animationSpec
    if (animationSpec != null && (visible || placeholderAlpha >= 0.01f)) {
        val infiniteTransition = rememberInfiniteTransition(label = "placeholder_highlight")
        highlightProgress = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = animationSpec,
            label = "placeholder_highlight_progress",
        ).value
    }

    val paint = remember { Paint() }
    remember(placeholderColor, placeholderShape, highlight) {
        drawWithContent {
            if (contentAlpha in 0.01f..0.99f) {
                paint.alpha = contentAlpha
                withLayer(paint) {
                    with(this@drawWithContent) {
                        drawContent()
                    }
                }
            } else if (contentAlpha >= 0.99f) {
                drawContent()
            }

            if (placeholderAlpha in 0.01f..0.99f) {
                paint.alpha = placeholderAlpha
                withLayer(paint) {
                    drawPlaceholder(placeholderShape, placeholderColor, highlight, highlightProgress, cache)
                }
            } else if (placeholderAlpha >= 0.99f) {
                drawPlaceholder(placeholderShape, placeholderColor, highlight, highlightProgress, cache)
            }
        }
    }
}

private class OutlineCache {
    var outline: Outline? = null
    var size: Size? = null
    var layoutDirection: LayoutDirection? = null
}

private fun DrawScope.drawPlaceholder(
    shape: Shape,
    color: Color,
    highlight: PlaceholderHighlight?,
    progress: Float,
    cache: OutlineCache,
) {
    if (shape === RectangleShape) {
        drawRect(color = color)
        if (highlight != null) {
            drawRect(
                brush = highlight.brush(progress, size),
                alpha = highlight.alpha(progress),
            )
        }
        return
    }

    val outline = cache.outline.takeIf {
        size == cache.size && layoutDirection == cache.layoutDirection
    } ?: shape.createOutline(size, layoutDirection, this)
    cache.outline = outline
    cache.size = size
    cache.layoutDirection = layoutDirection

    drawOutline(outline = outline, color = color)
    if (highlight != null) {
        drawOutline(
            outline = outline,
            brush = highlight.brush(progress, size),
            alpha = highlight.alpha(progress),
        )
    }
}

private inline fun DrawScope.withLayer(
    paint: Paint,
    drawBlock: DrawScope.() -> Unit,
) = drawIntoCanvas { canvas ->
    canvas.saveLayer(size.toRect(), paint)
    drawBlock()
    canvas.restore()
}
