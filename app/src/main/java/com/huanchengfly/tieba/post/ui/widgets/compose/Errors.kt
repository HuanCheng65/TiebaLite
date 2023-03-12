package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.retrofit.exception.NoConnectivityException
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaApiException
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaNotLoggedInException
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorCode
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.BaseComposeActivity.Companion.LocalWindowSizeClass
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.windowsizeclass.WindowWidthSizeClass
import com.huanchengfly.tieba.post.ui.widgets.compose.states.StateScreenScope

@Composable
fun TipScreen(
    title: @Composable (ColumnScope.() -> Unit),
    modifier: Modifier = Modifier,
    image: @Composable (ColumnScope.() -> Unit) = {},
    message: @Composable (ColumnScope.() -> Unit) = {},
    actions: @Composable (ColumnScope.() -> Unit) = {}
) {
    val widthFraction =
        if (LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Compact) 0.9f else 0.5f
    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth(fraction = widthFraction)
                .padding(16.dp)
                .verticalScroll(
                    rememberScrollState()
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterVertically)
        ) {
            image()
            ProvideTextStyle(
                value = MaterialTheme.typography.h6.copy(
                    color = ExtendedTheme.colors.text,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            ) {
                title()
            }
            ProvideTextStyle(
                value = MaterialTheme.typography.body1.copy(
                    color = ExtendedTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            ) {
                message()
            }
            actions()
        }
    }
}

enum class ErrorType {
    NETWORK,
    SERVER,
    NOT_LOGGED_IN,
    UNKNOWN
}

@Composable
fun StateScreenScope.ErrorScreen(
    error: Throwable,
    modifier: Modifier = Modifier,
    showReload: Boolean = true,
    actions: @Composable (ColumnScope.() -> Unit) = {},
) {
    ErrorTipScreen(
        error = error,
        modifier = modifier,
        actions = {
            if (showReload && canReload) {
                Button(onClick = { reload() }) {
                    Text(text = stringResource(id = R.string.btn_reload))
                }
            }

            actions()
        }
    )
}

@Composable
fun ErrorTipScreen(
    error: Throwable,
    modifier: Modifier = Modifier,
    actions: @Composable (ColumnScope.() -> Unit) = {},
) {
    val errorType = when (error) {
        is NoConnectivityException -> ErrorType.NETWORK
        is TiebaApiException -> ErrorType.SERVER
        is TiebaNotLoggedInException -> ErrorType.NOT_LOGGED_IN
        else -> ErrorType.UNKNOWN
    }
    val errorMessage = error.getErrorMessage()
    val errorCode = error.getErrorCode()
    ErrorTipScreen(
        errorType = errorType,
        errorMessage = errorMessage,
        modifier = modifier,
        errorCode = errorCode,
        actions = actions
    )
}

@Composable
fun ErrorTipScreen(
    errorType: ErrorType,
    errorMessage: String,
    modifier: Modifier = Modifier,
    errorCode: Int? = null,
    appendMessage: @Composable (ColumnScope.() -> Unit) = {},
    actions: @Composable (ColumnScope.() -> Unit) = {},
) {
    TipScreen(
        title = {
            when (errorType) {
                ErrorType.NETWORK -> {
                    Text(text = stringResource(id = R.string.title_no_internet_connectivity))
                }

                ErrorType.SERVER -> {
                    Text(text = stringResource(id = R.string.title_api_error))
                }

                ErrorType.UNKNOWN -> {
                    Text(text = stringResource(id = R.string.title_unknown_error))
                }

                ErrorType.NOT_LOGGED_IN -> {
                    Text(text = stringResource(id = R.string.title_not_logged_in))
                }
            }
        },
        modifier = modifier,
        image = {
            when (errorType) {
                ErrorType.NETWORK -> {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_no_internet))
                    LottieAnimation(
                        composition = composition,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f)
                    )
                }

                ErrorType.SERVER -> {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_error))
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f)
                    )
                }

                ErrorType.UNKNOWN -> {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_bug_hunting))
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f)
                    )
                }

                ErrorType.NOT_LOGGED_IN -> {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_astronaut))
                    LottieAnimation(
                        composition = composition,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f)
                    )
                }
            }
        },
        message = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (errorType) {
                    ErrorType.NETWORK -> {
                        Text(
                            text = stringResource(
                                id = R.string.message_no_internet_connectivity,
                                errorMessage
                            )
                        )
                    }

                    ErrorType.SERVER -> {
                        val errorCodeText = "($errorCode)".takeIf { errorCode != null }.orEmpty()
                        Text(text = "$errorMessage$errorCodeText")
                    }

                    ErrorType.UNKNOWN -> {
                        Text(text = stringResource(id = R.string.message_unknown_error))
                    }

                    ErrorType.NOT_LOGGED_IN -> {
                        Text(text = stringResource(id = R.string.message_not_logged_in))
                    }
                }
                appendMessage()
            }
        },
        actions = actions
    )
}