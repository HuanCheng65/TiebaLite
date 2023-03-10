package com.huanchengfly.tieba.post.ui.widgets.compose.states

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.widgets.compose.EmptyPlaceholder

val DefaultLoadingScreen: @Composable () -> Unit = {
    CircularProgressIndicator(modifier = Modifier.size(48.dp), color = MaterialTheme.colors.primary)
}

val DefaultEmptyScreen: @Composable () -> Unit = {
    EmptyPlaceholder()
}

val DefaultErrorScreen: @Composable () -> Unit = {
    Text(
        text = stringResource(id = R.string.error_tip),
        style = MaterialTheme.typography.body1,
        color = ExtendedTheme.colors.textSecondary
    )
}

@Composable
fun StateScreen(
    isEmpty: Boolean,
    isError: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    onReload: (() -> Unit)? = null,
    emptyScreen: @Composable () -> Unit = DefaultEmptyScreen,
    errorScreen: @Composable () -> Unit = DefaultErrorScreen,
    loadingScreen: @Composable () -> Unit = DefaultLoadingScreen,
    content: @Composable () -> Unit,
) {
    val clickableModifier = if (onReload != null) Modifier.clickable(
        enabled = isEmpty && !isLoading,
        onClick = onReload
    ) else Modifier
    Box(
        modifier = Modifier
            .fillMaxSize()
                then modifier
                then clickableModifier,
        contentAlignment = Alignment.Center
    ) {
        if (!isEmpty) {
            content()
        } else {
            if (isLoading) {
                loadingScreen()
            } else if (isError) {
                errorScreen()
            } else {
                emptyScreen()
            }
        }
    }
}