package com.huanchengfly.tieba.post.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.utils.ThemeUtil

@Composable
fun Chip(
    text: String,
    invertColor: Boolean,
    modifier: Modifier
) {
    Text(
        color = if (invertColor)
            if (ThemeUtil.isNightMode()) MaterialTheme.colors.secondary else MaterialTheme.colors.onSecondary
        else ExtendedTheme.colors.onChip,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        text = text,
        modifier = Modifier
            .padding(start = 16.dp)
            .clip(RoundedCornerShape(100))
            .then(modifier)
            .background(
                color = if (invertColor)
                    if (ThemeUtil.isNightMode()) MaterialTheme.colors.secondary.copy(alpha = 0.3f) else MaterialTheme.colors.secondary
                else ExtendedTheme.colors.chip
            )
            .padding(horizontal = 16.dp, vertical = 4.dp)
    )
}