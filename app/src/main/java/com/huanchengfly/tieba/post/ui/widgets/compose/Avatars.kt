package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.huanchengfly.tieba.post.utils.ImageUtil

object Sizes {
    val Small = 36.dp
    val Middle = 48.dp
    val Large = 56.dp
}

@Composable
fun AvatarIcon(
    icon: ImageVector,
    size: Dp,
    contentDescription: String?,
    iconSize: Dp = 24.dp,
    color: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
    backgroundColor: Color = Color.Transparent,
    shape: Shape = CircleShape,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = color,
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(color = backgroundColor)
            .padding((size - iconSize) / 2),
    )
}

@Composable
fun Avatar(
    data: Any?,
    size: Dp,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current)
                .data(data)
                .apply {
                    placeholder(ImageUtil.getPlaceHolder(LocalContext.current, 0))
                    crossfade(true)
                }
                .build()
        ),
        contentDescription = contentDescription,
        modifier = modifier.size(size).clip(CircleShape),
    )
}