package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.huanchengfly.tieba.post.arch.BaseComposeActivity.Companion.LocalWindowSizeClass
import com.huanchengfly.tieba.post.ui.common.windowsizeclass.WindowWidthSizeClass

@Composable
fun Container(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val windowWidthSizeClass = LocalWindowSizeClass.current.widthSizeClass
    val widthFraction = remember(windowWidthSizeClass) {
        when (windowWidthSizeClass) {
            WindowWidthSizeClass.Medium -> 0.87f
            WindowWidthSizeClass.Expanded -> 0.75f
            else -> 1f
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = modifier.fillMaxWidth(widthFraction),
        ) {
            content()
        }
    }
}