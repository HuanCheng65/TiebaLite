package com.huanchengfly.tieba.post.ui.widgets.compose

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private const val TAG = "UniversalScrollBox"

private fun TwoFloats(width: Float, height: Float) = androidx.compose.ui.geometry.Size(width = width, height = height)

/**
 * 注意 content 中一定要有东西，没有做判空保护，会数组越界
 */
@Composable
fun UniversalScrollBox(
    modifier: Modifier = Modifier,
    scrollBarStroke: Dp = 2.dp,
    scrollBarColor: Color = MaterialTheme.colors.secondary.copy(alpha = 0.5F),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
    ) {
        var outerSize by remember { mutableStateOf(IntSize(width = 0, height = 0)) } // 外层宽高，即内容可见的宽度
        var sizeRatio by remember { mutableStateOf(TwoFloats(width = 0F, height = 0F)) } // 内外大小比
        var barSize by remember { mutableStateOf(TwoFloats(width = 0F, height = 0F)) } // 水平滚动条宽度、垂直滚动条高度，$\frac{滚动条宽度}{可见内容宽度} = \frac{可见内容宽度}{内容总宽度}$
        var dragOffset by remember { mutableStateOf(TwoFloats(width = 0F, height = 0F)) } // 滚动条在水平、垂直方向拖动的距离
        /**
         * 水平滚动条会遮挡垂直方向内容，因此，当水平滚动条存在时，需要设置垂直方向 padding，垂直滚动条同理。
         * 当 content 变化，导致首次出现滚动条时，padding 随之发生变化，绘制时 outerSize 变化，从而可能使得另一个滚动条因此出现，继续导致 padding 变化，outerSize 变化，滚动条宽度变化。
         * 但是这几次变化之后， padding 不会再改变，因而滚动条不会继续变化，变化终止。虽然第一次变化的时候，可以预测后续变化，但是懒得计算了。
         */
        Layout(
            modifier = Modifier.fillMaxSize()
                .padding(bottom = if (barSize.width > 0) 16.dp else 0.dp, end = if (barSize.height > 0) 16.dp else 0.dp)
                .clipToBounds()
                .align(Alignment.TopStart)
                .wrapContentSize(unbounded = false),
            content = content
        ) { measurables, constraints -> // 外层 modifier 使用 unbounded false，可以通过 constraints.maxWidth maxHeight 算出外层的最大大小，注意这个 Size 是 padding 之后的
            outerSize = IntSize(width = constraints.maxWidth, height = constraints.maxHeight)
            Log.d(TAG, "outerSize $outerSize")
            val placeables = measurables.map { measurable ->
                // 实际计算 子 Node 的时候，使用 maxWidth = Int.MAX_VALUE，可以得到 子 Node 的真实宽度
                measurable.measure(
                    constraints = constraints.copy(
                        maxHeight = Int.MAX_VALUE,
                        maxWidth = Int.MAX_VALUE
                    )
                )
            }
            val innerSize = IntSize(width = placeables[0].width, height = placeables[0].height)

            // 计算是否需要水平、垂直滚动条
            val needWidth = innerSize.width > outerSize.width
            val needHeight = innerSize.height > outerSize.height
            sizeRatio = TwoFloats(
                width = if (needWidth) innerSize.width.toFloat() / outerSize.width.toFloat() else 0F,
                height = if (needHeight) innerSize.height.toFloat() / outerSize.height.toFloat() else 0F,
            )
            barSize = TwoFloats(
                width = if (needWidth) (outerSize.width * outerSize.width).toFloat() / innerSize.width else 0F,
                height = if (needHeight) (outerSize.height * outerSize.height).toFloat() / innerSize.height else 0F
            )
            Log.d(
                TAG,
                "bar(width: ${barSize.width}, height ${barSize.height}) ratio(width: ${sizeRatio.width}, height ${sizeRatio.height})"
            )
            layout(width = outerSize.width, height = outerSize.height) {
                placeables.forEach {
                    it.place(
                        x = (-dragOffset.width * sizeRatio.width).toInt(),
                        y = (-dragOffset.height * sizeRatio.height).toInt(),
                        zIndex = 0F
                    )
                }
            }
        }
        if (barSize.width > 0) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .height(scrollBarStroke)
                    .align(Alignment.BottomStart)
                    .background(Color.Transparent)
            ) {
                Box(
                    modifier = Modifier.align(Alignment.BottomStart)
                        .fillMaxHeight()
                        .offset { IntOffset(dragOffset.width.roundToInt(), 0) }
                        .width(barSize.width.dp)
                        .clip(RoundedCornerShape(4.dp)) // 注意先 clip 再 background
                        .background(scrollBarColor) // 注意先 offset 再 background
                        .draggable(state = rememberDraggableState {
                            var widthOffset = dragOffset.width
                            widthOffset += it
                            // 限制拖动不要超过两端
                            // 注意水平、垂直滚动条会互相影响，当两方同时存在时，应该控制大小，不要相交在 BottomEnd
                            if (widthOffset < 0) {
                                widthOffset = 0F
                            } else if (widthOffset > outerSize.width - barSize.width) {
                                widthOffset = (outerSize.width - barSize.width)
                            }
                            dragOffset = TwoFloats(
                                width = widthOffset,
                                height = dragOffset.height
                            )

                        }, orientation = Orientation.Horizontal)
                )
            }
        }
        if (barSize.height > 0) {
            Box(
                modifier = Modifier.fillMaxHeight()
                    .width(scrollBarStroke)
                    .align(Alignment.TopEnd)
                    .background(Color.Transparent)
            ) {
                Box(
                    modifier = Modifier.align(Alignment.TopEnd)
                        .fillMaxWidth()
                        .offset { IntOffset(0, dragOffset.height.roundToInt()) }
                        .height(barSize.height.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(scrollBarColor)
                        .draggable(state = rememberDraggableState {
                            var heightOffset = dragOffset.height
                            heightOffset += it
                            if (heightOffset < 0) {
                                heightOffset = 0F
                            } else if (heightOffset > outerSize.height - barSize.height) {
                                heightOffset = (outerSize.height - barSize.height)
                            }
                            dragOffset = TwoFloats(
                                width = dragOffset.width,
                                height = heightOffset
                            )

                        }, orientation = Orientation.Vertical)
                )
            }
        }
    }
}
