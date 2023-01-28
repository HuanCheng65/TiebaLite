package com.huanchengfly.tieba.post.ui.widgets.compose.picker

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.pxToDp
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun <T> WheelPicker(
    modifier: Modifier = Modifier,
    items: List<T> = emptyList(),
    selectedItem: T? = null,
    itemHeight: Dp = 32.dp,
    divider: NumberPickerDivider = NumberPickerDivider(),
    itemStyles: ItemStyles = ItemStyles(),
    onItemChanged: (T) -> Unit = {},
    itemToString: (T) -> String = { it.toString() }
) {
    val scope = rememberCoroutineScope()
    var currItem by remember { mutableStateOf(selectedItem) }
    var listHeightInPixels by remember { mutableStateOf(0) }
    var itemHeightInPixels by remember { mutableStateOf(0) }

    val listState = rememberSaveable(saver = LazyListState.Saver) {
        if (items.isNotEmpty()) {
            val centerList = Int.MAX_VALUE / 2 - (Int.MAX_VALUE / 2) % items.size
            val selectedIndex = selectedItem?.let { items.indexOf(selectedItem) } ?: 0
            LazyListState(
                firstVisibleItemIndex = centerList + selectedIndex
            )
        } else {
            LazyListState()
        }
    }

    if (listState.isScrollInProgress.not() && itemHeightInPixels > 0) {
        val needScrollTop = -listState.firstVisibleItemScrollOffset + itemHeightInPixels / 2

        if (abs(listState.firstVisibleItemScrollOffset - itemHeightInPixels / 2) > 1) {

            LaunchedEffect(key1 = listState) {
                scope.launch {
                    listState.animateScrollBy(needScrollTop.toFloat())
                    if (items[listState.firstVisibleItemIndex % items.size] != currItem) {
                        currItem = items[listState.firstVisibleItemIndex % items.size]
                        onItemChanged(currItem!!)
                    }
                }
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (items.isNotEmpty()) {
            LazyColumn(
                contentPadding = PaddingValues(top = (listHeightInPixels / 2).pxToDp().dp),
                state = listState,
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .onGloballyPositioned {
                        if (listHeightInPixels < itemHeightInPixels) {
                            listHeightInPixels = it.size.height
                        }
                    }
            ) {
                items(Int.MAX_VALUE) { i ->
                    val index = i % items.size
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth(1f)
                            .height(itemHeight)
                            .onGloballyPositioned {
                                if (itemHeightInPixels == 0) {
                                    itemHeightInPixels = it.size.height
                                }
                            }
                    ) {
                        Text(
                            text = itemToString(items[index]),
                            style = if (i == listState.firstVisibleItemIndex) itemStyles.selectedTextStyle else itemStyles.defaultTextStyle
                        )
                    }
                }
            }
        }
        if (divider.showed) {
            Divider(
                color = divider.color,
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .offset(y = -itemHeight / 2)
                    .offset(x = -divider.indent),
                thickness = divider.thickness,
                startIndent = divider.indent * 2
            )
            Divider(
                color = divider.color, modifier = Modifier
                    .fillMaxWidth(1f)
                    .offset(y = itemHeight / 2)
                    .offset(x = -divider.indent),
                thickness = divider.thickness,
                startIndent = divider.indent * 2
            )
        }
    }
}

@Immutable
class NumberPickerDivider(
    val showed: Boolean = false,
    val color: Color = Color.Transparent,
    val thickness: Dp = 1.dp,
    val indent: Dp = 0.dp
)

@Immutable
class ItemStyles(
    val defaultTextStyle: TextStyle = TextStyle.Default,
    val selectedTextStyle: TextStyle = TextStyle.Default
)