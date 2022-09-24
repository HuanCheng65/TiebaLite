package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MenuScope(
    private val menuState: MenuState,
    private val onDismiss: (() -> Unit)? = null,
) {
    fun dismiss() {
        onDismiss?.invoke()
        menuState.expanded = false
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClickMenu(
    menuState: MenuState = rememberMenuState(),
    menuContent: @Composable MenuScope.() -> Unit,
    shape: Shape = RoundedCornerShape(14.dp),
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable MenuScope.() -> Unit
) {
    val menuScope = MenuScope(menuState, onDismiss)
    val coroutineScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val indication = LocalIndication.current
    coroutineScope.launch {
        interactionSource.interactions
            .filterIsInstance<PressInteraction.Press>()
            .collect {
                menuState.offset = it.pressPosition
            }
    }
    Box(
        modifier = Modifier
            .combinedClickable(
                interactionSource = interactionSource,
                indication = indication,
                onClick = {
                    menuState.expanded = true
                }
            )
    ) {
        menuScope.content()
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        menuState.offset.x.roundToInt(),
                        menuState.offset.y.roundToInt()
                    )
                }
                .clip(shape)
        ) {
            DropdownMenu(
                expanded = menuState.expanded,
                onDismissRequest = { menuScope.dismiss() },
                modifier = modifier.background(color = MaterialTheme.colors.surface)
            ) {
                ProvideContentColor(color = ExtendedTheme.colors.text) {
                    menuScope.menuContent()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LongClickMenu(
    menuState: MenuState = rememberMenuState(),
    menuContent: @Composable ColumnScope.() -> Unit,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(14.dp),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val indication = LocalIndication.current
    coroutineScope.launch {
        interactionSource.interactions
            .filterIsInstance<PressInteraction.Press>()
            .collect {
                menuState.offset = it.pressPosition
            }
    }
    Box(
        modifier = Modifier
            .combinedClickable(
                interactionSource = interactionSource,
                indication = indication,
                onLongClick = {
                    menuState.expanded = true
                }
            ) {
                onClick?.invoke()
            }
    ) {
        content()
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        menuState.offset.x.roundToInt(),
                        menuState.offset.y.roundToInt()
                    )
                }
                .clip(shape)
        ) {
            DropdownMenu(
                expanded = menuState.expanded,
                onDismissRequest = { menuState.expanded = false },
                modifier = modifier.background(color = MaterialTheme.colors.surface)
            ) {
                ProvideContentColor(color = ExtendedTheme.colors.text) {
                    menuContent()
                }
            }
        }
    }
}

@Composable
fun rememberMenuState(): MenuState {
    return rememberSaveable(saver = MenuState.Saver) {
        MenuState()
    }
}

@Stable
class MenuState(
    expanded: Boolean = false,
    offsetX: Float = 0f,
    offsetY: Float = 0f,
) {
    private var _expanded by mutableStateOf(expanded)

    var expanded: Boolean
        get() = _expanded
        set(value) {
            if (value != _expanded) {
                _expanded = value
            }
        }

    private var _offset by mutableStateOf(Offset(offsetX, offsetY))

    var offset: Offset
        get() = _offset
        set(value) {
            if (value != _offset) {
                _offset = value
            }
        }

    companion object {
        val Saver: Saver<MenuState, *> = listSaver(
            save = {
                listOf<Any>(
                    it.expanded,
                    it.offset.x,
                    it.offset.y
                )
            },
            restore = {
                MenuState(
                    expanded = it[0] as Boolean,
                    offsetX = it[1] as Float,
                    offsetY = it[2] as Float,
                )
            }
        )
    }
}