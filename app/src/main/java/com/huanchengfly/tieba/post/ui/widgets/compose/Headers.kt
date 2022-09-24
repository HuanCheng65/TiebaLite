package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme

@Composable
fun UserHeader(
    avatar: @Composable () -> Unit,
    name: @Composable () -> Unit,
    desc: @Composable () -> Unit,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable (RowScope.() -> Unit)? = null,
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        )
    } else Modifier
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = clickableModifier,
        ) {
            avatar()
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(modifier = clickableModifier) {
                ProvideTextStyle(value = MaterialTheme.typography.subtitle1.merge(TextStyle(color = ExtendedTheme.colors.text))) {
                    name()
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Box(modifier = clickableModifier) {
                ProvideTextStyle(value = MaterialTheme.typography.caption.merge(TextStyle(color = ExtendedTheme.colors.textSecondary))) {
                    desc()
                }
            }
        }
        content?.invoke(this)
    }
}