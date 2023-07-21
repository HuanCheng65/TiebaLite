package com.huanchengfly.tieba.post.ui.widgets

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.compose.invertChipBackground
import com.huanchengfly.tieba.post.ui.common.theme.compose.invertChipContent
import com.huanchengfly.tieba.post.ui.widgets.compose.ProvideContentColor

@Composable
fun Chip(
    text: String,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    invertColor: Boolean = false,
    shape: Shape = RoundedCornerShape(100),
) {
    val color =
        if (invertColor) ExtendedTheme.colors.invertChipContent else ExtendedTheme.colors.onChip
    val backgroundColor =
        if (invertColor) ExtendedTheme.colors.invertChipBackground else ExtendedTheme.colors.chip

    val animatedColor by animateColorAsState(targetValue = color, label = "ChipColor")
    val animatedBackgroundColor by animateColorAsState(
        targetValue = backgroundColor,
        label = "ChipBackgroundColor"
    )

    ProvideContentColor(color = animatedColor) {
        Row(
            modifier = modifier
                .clip(shape)
                .background(color = animatedBackgroundColor)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon?.invoke()
            Text(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                text = text
            )
        }
    }
}