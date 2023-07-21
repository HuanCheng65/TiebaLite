package com.huanchengfly.tieba.post.ui.widgets.compose.picker

import androidx.compose.material.ContentAlpha
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme

private class DefaultPickerColors(
    private val itemColor: Color,
    private val disabledItemColor: Color,
    private val selectedItemColor: Color,
    private val disabledSelectedItemColor: Color
) : PickerColors {

    @Composable
    override fun itemColor(enabled: Boolean): State<Color> =
        rememberUpdatedState(if (enabled) itemColor else disabledItemColor)

    @Composable
    override fun selectedItemColor(enabled: Boolean): State<Color> =
        rememberUpdatedState(if (enabled) selectedItemColor else disabledSelectedItemColor)
}

@Stable
interface PickerColors {
    @Composable
    fun itemColor(enabled: Boolean): State<Color>

    @Composable
    fun selectedItemColor(enabled: Boolean): State<Color>
}

object PickerDefaults {
    @Composable
    fun pickerColors(
        itemColor: Color = ExtendedTheme.colors.text,
        disabledItemColor: Color = itemColor.copy(alpha = ContentAlpha.disabled),
        selectedItemColor: Color = ExtendedTheme.colors.accent,
        disabledSelectedItemColor: Color = disabledItemColor
    ): PickerColors =
        DefaultPickerColors(
            itemColor = itemColor,
            disabledItemColor = disabledItemColor,
            selectedItemColor = selectedItemColor,
            disabledSelectedItemColor = disabledSelectedItemColor
        )
}