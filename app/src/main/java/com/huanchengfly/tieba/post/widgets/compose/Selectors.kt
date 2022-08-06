package com.huanchengfly.tieba.post.widgets.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme

@Composable
fun <ItemValue> SingleSelector(
    itemTitles: List<String>,
    itemValues: List<ItemValue>,
    selectedPosition: Int,
    onItemSelected: (position: Int, title: String, value: ItemValue, changed: Boolean) -> Unit,
    selectedIndicator: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = stringResource(id = R.string.desc_checked)
        )
    },
    colors: SelectorColors = SelectorDefaults.selectorColors(),
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
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
                    .padding(vertical = 16.dp, horizontal = 24.dp)
            ) {
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

private class DefaultSelectorColors(
    private val itemColor: Color,
    private val disabledItemColor: Color,
    private val selectedItemColor: Color,
    private val disabledSelectedItemColor: Color
) : SelectorColors {

    @Composable
    override fun itemColor(enabled: Boolean): State<Color> =
        rememberUpdatedState(if (enabled) itemColor else disabledItemColor)

    @Composable
    override fun selectedItemColor(enabled: Boolean): State<Color> =
        rememberUpdatedState(if (enabled) selectedItemColor else disabledSelectedItemColor)
}

interface SelectorColors {
    @Composable
    fun itemColor(enabled: Boolean): State<Color>

    @Composable
    fun selectedItemColor(enabled: Boolean): State<Color>
}


object SelectorDefaults {
    @Composable
    fun selectorColors(
        itemColor: Color = ExtendedTheme.colors.text,
        disabledItemColor: Color = itemColor.copy(alpha = ContentAlpha.disabled),
        selectedItemColor: Color = ExtendedTheme.colors.primary,
        disabledSelectedItemColor: Color = disabledItemColor
    ): SelectorColors =
        DefaultSelectorColors(
            itemColor = itemColor,
            disabledItemColor = disabledItemColor,
            selectedItemColor = selectedItemColor,
            disabledSelectedItemColor = disabledSelectedItemColor
        )
}