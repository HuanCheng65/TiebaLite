package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme

@Composable
fun ListMenuItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    iconColor: Color = ExtendedTheme.colors.accent,
    onClick: (() -> Unit)? = null,
    customContent: @Composable (RowScope.() -> Unit)? = null,
) {
    val menuModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .then(menuModifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Icon(imageVector = icon, contentDescription = text, tint = iconColor)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(16.dp))
        customContent?.invoke(this)
    }
}