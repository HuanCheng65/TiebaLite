package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Indication
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.compose.menuBackground
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
    modifier: Modifier = Modifier,
    menuShape: Shape = RoundedCornerShape(14.dp),
    triggerShape: Shape? = null,
    onDismiss: (() -> Unit)? = null,
    content: @Composable MenuScope.() -> Unit
) {
    val menuScope = MenuScope(menuState, onDismiss)
    val interactionSource = remember { MutableInteractionSource() }
    val indication = LocalIndication.current
    LaunchedEffect(key1 = null) {
        launch {
            interactionSource.interactions
                .filterIsInstance<PressInteraction.Press>()
                .collect {
                    menuState.offset = it.pressPosition
                }
        }
    }
    Box {
        val triggerModifier = if (triggerShape != null) Modifier.clip(triggerShape) else Modifier
        Box(
            modifier = triggerModifier
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = indication,
                    onClick = {
                        menuState.expanded = true
                    }
                )
        ) {
            menuScope.content()
        }
        Box(
            modifier = Modifier
                .clip(menuShape)
                .offset {
                    IntOffset(
                        menuState.offset.x.roundToInt(),
                        menuState.offset.y.roundToInt()
                    )
                }
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
    menuContent: @Composable (ColumnScope.() -> Unit),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    menuState: MenuState = rememberMenuState(),
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(14.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    indication: Indication? = LocalIndication.current,
    content: @Composable () -> Unit,
) {
    LaunchedEffect(Unit) {
        launch {
            interactionSource.interactions
                .filterIsInstance<PressInteraction.Press>()
                .collect {
                    menuState.offset = it.pressPosition
                }
        }
    }
    Box(
        modifier = modifier
            .combinedClickable(
                interactionSource = interactionSource,
                indication = indication,
                enabled = enabled,
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
        ) {
            DropdownMenu(
                expanded = menuState.expanded,
                onDismissRequest = { menuState.expanded = false },
                modifier = Modifier
                    .clip(shape)
                    .background(color = ExtendedTheme.colors.menuBackground)
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
    return rememberSaveable(
        saver = MenuState.Saver,
        init = { MenuState() }
    )
}

@Stable
class MenuState {
    private var _expanded by mutableStateOf(false)

    fun toggle() {
        expanded = !expanded
    }

    fun show() {
        expanded = true
    }

    fun dismiss() {
        expanded = false
    }

    var expanded: Boolean
        get() = _expanded
        set(value) {
            if (value != _expanded) {
                _expanded = value
            }
        }

    private var _offset by mutableStateOf(Offset(0f, 0f))

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
                MenuState().apply {
                    expanded = it[0] as Boolean
                    offset = Offset(it[1] as Float, it[2] as Float)
                }
            }
        )
    }
}