package com.huanchengfly.tieba.post.ui.widgets.compose.video

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun ShadowedIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconSize: Dp = 48.dp,
) {
    Box(modifier = modifier) {
        Icon(
            imageVector = icon,
            tint = Color.Black.copy(alpha = 0.3f),
            modifier = Modifier
                .size(iconSize)
                .offset(2.dp, 2.dp)
                .then(modifier),
            contentDescription = null
        )
        Icon(
            imageVector = icon,
            modifier = Modifier.size(iconSize),
            contentDescription = null
        )
    }
}