package com.huanchengfly.tieba.post.ui.widgets.compose.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.widgets.compose.ProvideContentColor
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

@Composable
fun <ItemValue> ListSinglePicker(
    itemTitles: ImmutableList<String>,
    itemValues: ImmutableList<ItemValue>,
    selectedPosition: Int,
    onItemSelected: (position: Int, title: String, value: ItemValue, changed: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    itemIcons: ImmutableMap<ItemValue, @Composable () -> Unit> = persistentMapOf(),
    selectedIndicator: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = stringResource(id = R.string.desc_checked)
        )
    },
    colors: PickerColors = PickerDefaults.pickerColors(),
    enabled: Boolean = true,
) {
    if (itemTitles.size != itemValues.size) error("titles and values do not match!")
    Column(modifier = modifier) {
        repeat(itemTitles.size) {
            val selected = it == selectedPosition
            Row(
                modifier = Modifier
                    .background(
                        color = colors.selectedItemColor(enabled = enabled).value.copy(
                            alpha = if (selectedPosition == it) 0.1f else 0f
                        )
                    )
                    .clickable(enabled = enabled) {
                        onItemSelected(
                            it,
                            itemTitles[it],
                            itemValues[it],
                            it != selectedPosition
                        )
                    }
                    .padding(vertical = 16.dp, horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemIcons[itemValues[it]]?.invoke()
                ProvideContentColor(
                    color = if (selected) colors.selectedItemColor(enabled).value else colors.itemColor(
                        enabled
                    ).value
                ) {
                    CompositionLocalProvider(LocalContentAlpha provides 1f) {
                        Text(
                            text = itemTitles[it],
                            modifier = Modifier.weight(1f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (selectedPosition == it) {
                            Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                                ProvideContentColor(color = colors.selectedItemColor(enabled = enabled).value) {
                                    selectedIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


