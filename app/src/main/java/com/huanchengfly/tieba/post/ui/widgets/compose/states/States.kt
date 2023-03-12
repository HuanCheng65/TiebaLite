package com.huanchengfly.tieba.post.ui.widgets.compose.states

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.widgets.compose.Button
import com.huanchengfly.tieba.post.ui.widgets.compose.TipScreen

val DefaultLoadingScreen: @Composable StateScreenScope.() -> Unit = {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading_paperplane))
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f)
    )
//    CircularProgressIndicator(modifier = Modifier.size(48.dp), color = MaterialTheme.colors.primary)
}

val DefaultEmptyScreen: @Composable StateScreenScope.() -> Unit = {
    TipScreen(
        title = { Text(text = stringResource(id = R.string.title_empty)) },
        image = {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_empty_box))
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f)
            )
        },
        actions = {
            if (canReload) {
                Button(onClick = { reload() }) {
                    Text(text = stringResource(id = R.string.btn_refresh))
                }
            }
        }
    )
}

val DefaultErrorScreen: @Composable StateScreenScope.() -> Unit = {
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
    clickToReload: Boolean = false,
    emptyScreen: @Composable StateScreenScope.() -> Unit = DefaultEmptyScreen,
    errorScreen: @Composable StateScreenScope.() -> Unit = DefaultErrorScreen,
    loadingScreen: @Composable StateScreenScope.() -> Unit = DefaultLoadingScreen,
    content: @Composable StateScreenScope.() -> Unit,
) {
    val stateScreenScope = remember(key1 = onReload) { StateScreenScope(onReload) }
    val clickableModifier = if (onReload != null && clickToReload) Modifier.clickable(
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
            stateScreenScope.content()
        } else {
            if (isLoading) {
                stateScreenScope.loadingScreen()
            } else if (isError) {
                stateScreenScope.errorScreen()
            } else {
                stateScreenScope.emptyScreen()
            }
        }
    }
}

class StateScreenScope(
    private val onReload: (() -> Unit)? = null
) {
    val canReload: Boolean
        get() = onReload != null

    fun reload() {
        onReload?.invoke()
    }
}