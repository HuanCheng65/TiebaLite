package com.huanchengfly.tieba.post.ui.widgets.compose.dialogs

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay


@Composable
fun getDialogWindow(): Window? = (LocalView.current.parent as? DialogWindowProvider)?.window

@Composable
fun getActivityWindow(): Window? = LocalView.current.context.getActivityWindow()

tailrec fun Context.getActivityWindow(): Window? = when (this) {
    is Activity -> window
    is ContextWrapper -> baseContext.getActivityWindow()
    else -> null
}

private const val DefaultDurationMillis: Int = 250

@Composable
private fun DialogFullScreen(
    isActiveClose: Boolean,
    onDismissRequest: () -> Unit,
    properties: AnyPopDialogProperties,
    content: @Composable () -> Unit,
) {
    var isAnimateLayout by remember {
        mutableStateOf(false)
    }
    var isBackPress by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(isActiveClose) {
        if (isActiveClose) {
            isBackPress = true
            isAnimateLayout = false
        }
    }

    val handleBackPress = {
        if (!isBackPress) {
            isBackPress = true
            isAnimateLayout = false
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            // 这里不使用新的测量规范，不能设置为false
            usePlatformDefaultWidth = true,
            decorFitsSystemWindows = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            securePolicy = properties.securePolicy
        ),
        content = {
            val animColor = remember { Animatable(Color.Transparent) }
            LaunchedEffect(isAnimateLayout) {
                if (properties.backgroundDimEnabled) {
                    animColor.animateTo(
                        if (isAnimateLayout) Color.Black.copy(alpha = 0.45F) else Color.Transparent,
                        animationSpec = tween(properties.durationMillis)
                    )
                } else {
                    delay(properties.durationMillis.toLong())
                }
                if (!isAnimateLayout) {
                    onDismissRequest.invoke()
                }
            }
            val activityWindow = getActivityWindow()
            val dialogWindow = getDialogWindow()
            val parentView = LocalView.current.parent as View
            SideEffect {
                if (activityWindow != null && dialogWindow != null && !isBackPress && !isAnimateLayout) {
                    val attributes = WindowManager.LayoutParams()
                    attributes.copyFrom(activityWindow.attributes)
                    attributes.type = dialogWindow.attributes.type
                    dialogWindow.attributes = attributes

                    dialogWindow.setLayout(
                        activityWindow.decorView.width,
                        activityWindow.decorView.height
                    )
                    WindowCompat.getInsetsController(dialogWindow, parentView)
                        .isAppearanceLightNavigationBars =
                        properties.isAppearanceLightNavigationBars
                    isAnimateLayout = true
                }
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = when (properties.direction) {
                    DirectionState.TOP -> Alignment.TopCenter
                    DirectionState.LEFT -> Alignment.CenterStart
                    DirectionState.RIGHT -> Alignment.CenterEnd
                    DirectionState.CENTER -> Alignment.Center
                    else -> Alignment.BottomCenter
                }
            ) {
                Spacer(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(animColor.value)
                        .clickOutSideModifier(
                            dismissOnClickOutside = properties.dismissOnClickOutside,
                            onTap = handleBackPress
                        )
                )
                AnimatedVisibility(
                    modifier = Modifier.pointerInput(Unit) {},
                    visible = isAnimateLayout,
                    enter = when (properties.direction) {
                        DirectionState.TOP -> fadeIn() + slideInVertically(initialOffsetY = { -it })
                        DirectionState.LEFT -> fadeIn() + slideInHorizontally(initialOffsetX = { -it })
                        DirectionState.RIGHT -> fadeIn() + slideInHorizontally(initialOffsetX = { it })
                        DirectionState.CENTER -> fadeIn() + scaleIn(initialScale = 0.8F)
                        else -> fadeIn() + slideInVertically(initialOffsetY = { it })
                    },
                    exit = when (properties.direction) {
                        DirectionState.TOP -> fadeOut() + slideOutVertically(targetOffsetY = { -it })
                        DirectionState.LEFT -> fadeOut() + slideOutHorizontally(targetOffsetX = { -it })
                        DirectionState.RIGHT -> fadeOut() + slideOutHorizontally(targetOffsetX = { it })
                        DirectionState.CENTER -> fadeOut() + scaleOut(targetScale = 0.8F)
                        else -> fadeOut() + slideOutVertically(targetOffsetY = { it })
                    }
                ) {
                    content()
                }
            }
            BackHandler(enabled = properties.dismissOnBackPress, onBack = handleBackPress)
        }
    )
}

/**
 * @author 被风吹过的夏天
 * @see <a href="https://github.com/TheMelody/AnyPopDialog-Compose">https://github.com/TheMelody/AnyPopDialog-Compose</a>
 * @param isActiveClose 设置为true可触发动画关闭Dialog，动画完自动触发[onDismiss]
 * @param properties Dialog相关配置
 * @param onDismiss Dialog关闭的回调
 * @param content 可组合项视图
 */
@Composable
fun AnyPopDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isActiveClose: Boolean = false,
    properties: AnyPopDialogProperties = AnyPopDialogProperties(direction = DirectionState.BOTTOM),
    content: @Composable () -> Unit,
) {
    DialogFullScreen(
        isActiveClose = isActiveClose,
        onDismissRequest = onDismiss,
        properties = properties
    ) {
        val innerModifier = if (properties.imePadding) {
            modifier.imePadding()
        } else {
            modifier
        }
        Column(
            modifier = innerModifier
                .systemBarsPadding()
        ) {
            content()
        }
    }
}

/**
 * @param dismissOnBackPress 是否支持返回关闭Dialog
 * @param dismissOnClickOutside 是否支持空白区域点击关闭Dialog
 * @param isAppearanceLightNavigationBars 导航栏前景色是不是亮色
 * @param direction 当前对话框弹出的方向
 * @param backgroundDimEnabled 背景渐入检出开关
 * @param durationMillis 弹框消失和进入的时长
 * @param securePolicy 屏幕安全策略
 * @param imePadding 输入法弹出时是否自动调整布局
 */
@Immutable
class AnyPopDialogProperties(
    val direction: DirectionState = DirectionState.BOTTOM,
    val dismissOnBackPress: Boolean = true,
    val dismissOnClickOutside: Boolean = true,
    val isAppearanceLightNavigationBars: Boolean = true,
    val backgroundDimEnabled: Boolean = true,
    val durationMillis: Int = DefaultDurationMillis,
    val securePolicy: SecureFlagPolicy = SecureFlagPolicy.Inherit,
    val imePadding: Boolean = true,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AnyPopDialogProperties) return false

        if (dismissOnBackPress != other.dismissOnBackPress) return false
        if (isAppearanceLightNavigationBars != other.isAppearanceLightNavigationBars) return false
        if (dismissOnClickOutside != other.dismissOnClickOutside) return false
        if (direction != other.direction) return false
        if (backgroundDimEnabled != other.backgroundDimEnabled) return false
        if (durationMillis != other.durationMillis) return false
        return securePolicy == other.securePolicy
    }

    override fun hashCode(): Int {
        var result = dismissOnBackPress.hashCode()
        result = 31 * result + dismissOnClickOutside.hashCode()
        result = 31 * result + isAppearanceLightNavigationBars.hashCode()
        result = 31 * result + direction.hashCode()
        result = 31 * result + backgroundDimEnabled.hashCode()
        result = 31 * result + durationMillis.hashCode()
        result = 31 * result + securePolicy.hashCode()
        return result
    }
}

enum class DirectionState {
    TOP,
    LEFT,
    RIGHT,
    BOTTOM,
    CENTER
}

private fun Modifier.clickOutSideModifier(
    dismissOnClickOutside: Boolean,
    onTap: () -> Unit,
) = this.then(
    if (dismissOnClickOutside) {
        Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = {
                onTap()
            })
        }
    } else Modifier
)