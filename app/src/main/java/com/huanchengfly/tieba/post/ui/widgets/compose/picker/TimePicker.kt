package com.huanchengfly.tieba.post.ui.widgets.compose.picker

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun TimePicker(
    currentTime: String,
    onTimeChanged: (String) -> Unit,
    is24TimeFormat: Boolean,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 32.dp,
    divider: NumberPickerDivider = NumberPickerDivider(),
    itemStyles: ItemStyles = ItemStyles(
        defaultTextStyle = MaterialTheme.typography.body1.copy(
            color = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.medium),
            fontWeight = FontWeight.Normal
        ),
        selectedTextStyle = MaterialTheme.typography.h5.copy(
            color = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.high),
            fontWeight = FontWeight.Medium
        )
    )
) {

    val pickerTime by remember {
        mutableStateOf(
            PickerTime.parse(
                currentTime,
                is24TimeFormat = is24TimeFormat
            )
        )
    }

    val hours = (if (is24TimeFormat) (0..23) else (1..12)).toList()
    val minutes = (0..59).toList()

    Row(horizontalArrangement = Arrangement.Center, modifier = modifier.fillMaxWidth(1f)) {
        WheelPicker(
            itemHeight = itemHeight,
            divider = divider,
            itemStyles = itemStyles,
            items = hours,
            selectedItem = pickerTime.hours,
            itemToString = { if (is24TimeFormat) String.format("%02d", it) else it.toString() },
            modifier = Modifier
                .fillMaxHeight(1f)
                .fillMaxWidth(.3f),
            onItemChanged = {
                pickerTime.hours = it
                onTimeChanged(pickerTime.toString())
            }
        )
        WheelPicker(
            items = minutes,
            selectedItem = pickerTime.minutes,
            itemHeight = itemHeight,
            divider = divider,
            itemStyles = itemStyles,
            itemToString = { String.format("%02d", it) },
            modifier = Modifier
                .fillMaxHeight(1f)
                .fillMaxWidth(.3f),
            onItemChanged = {
                pickerTime.minutes = it
                onTimeChanged(pickerTime.toString())
            }

        )
        if (is24TimeFormat.not()) {
            AmPmPicker(
                itemHeight = itemHeight,
                divider = divider,
                itemStyles = itemStyles,
                modifier = Modifier
                    .fillMaxHeight(1f)
                    .fillMaxWidth(.3f),
                selectedItem = pickerTime.timesOfDay!!,
                onItemChanged = {
                    pickerTime.timesOfDay = it
                    onTimeChanged(pickerTime.toString())
                }
            )
        }
    }
}

@Composable
fun AmPmPicker(
    modifier: Modifier = Modifier,
    selectedItem: TimesOfDay,
    itemHeight: Dp = 32.dp,
    divider: NumberPickerDivider = NumberPickerDivider(showed = true, Color.Black),
    itemStyles: ItemStyles = ItemStyles(),
    onItemChanged: (TimesOfDay) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var currItem by remember { mutableStateOf(selectedItem) }
    var listHeightInPixels by remember { mutableStateOf(0) }
    var itemHeightInPixels by remember { mutableStateOf(0) }

    val items = TimesOfDay.values()

    val listState =
        rememberLazyListState(initialFirstVisibleItemIndex = items.indexOf(selectedItem))

    if (listState.isScrollInProgress.not() && itemHeightInPixels > 0 && listHeightInPixels > itemHeightInPixels) {
        if (listState.firstVisibleItemScrollOffset != 0) {
            LaunchedEffect(key1 = listState) {
                scope.launch {
                    listState.animateScrollBy(
                        (if (listState.firstVisibleItemScrollOffset <= itemHeightInPixels / 2)
                            -listState.firstVisibleItemScrollOffset - itemHeightInPixels
                        else listState.firstVisibleItemScrollOffset + itemHeightInPixels).toFloat()
                    )
                }
            }
        } else {
            if (items[listState.firstVisibleItemIndex] != currItem) {
                currItem = items[listState.firstVisibleItemIndex]
                onItemChanged(currItem)
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (items.isNotEmpty()) {
            LazyColumn(
                contentPadding = PaddingValues(top = itemHeight, bottom = itemHeight),
                state = listState,
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .height(itemHeight * (items.size + 1))
                    .onGloballyPositioned {
                        if (listHeightInPixels < itemHeightInPixels) {
                            listHeightInPixels = it.size.height

                            scope.launch {
                                listState.scrollToItem(items.indexOf(selectedItem))
                            }
                        }
                    }
            ) {
                items(items.size) { i ->
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
                            text = items[i].string,
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

enum class TimesOfDay(val string: String) {
    AM("AM"), PM("PM")
}

@Stable
data class PickerTime(
    var hours: Int,
    var minutes: Int,
    var timesOfDay: TimesOfDay? = null
) {
    companion object {
        fun parse(string: String, is24TimeFormat: Boolean): PickerTime {
            val is24 =
                listOf(TimesOfDay.AM, TimesOfDay.PM).none { string.endsWith(it.string, true) }
            val hours: Int
            val minutes: Int
            if (is24) {
                hours = string.split(":")[0].toInt()
                minutes = string.split(":")[1].toInt()
            } else {
                val timesOfDay =
                    TimesOfDay.values().first { it.string == string.split(":")[1].split(" ")[1] }
                hours = string.split(":")[0].toInt() + if (timesOfDay == TimesOfDay.PM) 12 else 0
                minutes = string.split(":")[1].split(" ")[0].toInt()
            }
            return if (is24TimeFormat) PickerTime(hours, minutes) else {
                val hours12 = if (hours == 0) 12 else if (hours > 12) hours - 12 else hours
                val timesOfDay = if (hours == 0 || hours > 12) TimesOfDay.PM else TimesOfDay.AM
                PickerTime(hours12, minutes, timesOfDay)
            }
        }
    }

    override fun toString(): String {
        return "${String.format("%02d", hours)}:${
            String.format(
                "%02d",
                minutes
            )
        }${" ".takeIf { timesOfDay != null }.orEmpty()}${timesOfDay?.string.orEmpty()}"
    }
}