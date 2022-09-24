package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun LazyLoad(
    loaded: Boolean,
    onLoad: () -> Unit,
) {
    LaunchedEffect(onLoad) {
        if (!loaded) onLoad()
    }
}