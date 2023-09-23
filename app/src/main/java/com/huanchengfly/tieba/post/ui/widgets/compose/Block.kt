package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.utils.appPreferences

@Composable
fun BlockTip(
    text: @Composable () -> Unit = { Text(text = stringResource(id = R.string.tip_blocked_content)) },
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(ExtendedTheme.colors.textSecondary.copy(alpha = 0.1f))
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        ProvideTextStyle(value = MaterialTheme.typography.caption) {
            ProvideContentColor(color = ExtendedTheme.colors.textSecondary) {
                text()
            }
        }
    }
}

@Composable
fun BlockableContent(
    blocked: Boolean,
    modifier: Modifier = Modifier,
    blockedTip: @Composable () -> Unit = { BlockTip() },
    hideBlockedContent: Boolean = LocalContext.current.appPreferences.hideBlockedContent,
    content: @Composable () -> Unit,
) {
    if (!blocked) {
        content()
    } else if (!hideBlockedContent) {
        Column(modifier = modifier) {
            blockedTip()
        }
    }
}