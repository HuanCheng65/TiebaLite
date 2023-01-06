package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme

@Composable
fun HorizontalDivider(
    modifier: Modifier = Modifier,
    color: Color = ExtendedTheme.colors.divider,
    height: Dp = 16.dp,
    width: Dp = 1.dp,
) {
    Box(
        modifier = modifier
            .height(height)
            .width(width)
            .background(color = color)
    )
}