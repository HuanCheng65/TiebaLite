package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.widgets.compose.dialogs.AnyPopDialog
import com.huanchengfly.tieba.post.ui.widgets.compose.dialogs.AnyPopDialogProperties
import com.huanchengfly.tieba.post.ui.widgets.compose.picker.TimePicker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DialogScope.DialogPositiveButton(
    text: String,
    onClick: () -> Unit = {}
) {
    TextButton(
        onClick = {
            onClick()
            dismiss()
        },
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(100),
        colors = ButtonDefaults.textButtonColors(
            backgroundColor = MaterialTheme.colors.secondary,
            contentColor = MaterialTheme.colors.onSecondary
        ),
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            modifier = Modifier.padding(vertical = 4.dp),
        )
    }
}

@Composable
fun DialogScope.DialogNegativeButton(
    text: String,
    onClick: (() -> Unit)? = null
) {
    TextButton(
        onClick = {
            dismiss()
            onClick?.invoke()
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(100),
        colors = ButtonDefaults.textButtonColors(
            backgroundColor = ExtendedTheme.colors.text.copy(
                alpha = 0.1f
            ),
            contentColor = ExtendedTheme.colors.text
        ),
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            modifier = Modifier.padding(vertical = 4.dp),
        )
    }
}

@Composable
fun TimePickerDialog(
    title: @Composable (DialogScope.() -> Unit),
    currentTime: String,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier,
    dialogState: DialogState = rememberDialogState(),
    onValueChange: ((String) -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    is24TimeFormat: Boolean = true,
    confirmText: String = stringResource(id = R.string.button_sure_default),
    cancelText: String = stringResource(id = R.string.button_cancel),
    content: @Composable (DialogScope.() -> Unit) = {},
) {
    var timeVal by remember { mutableStateOf(currentTime) }
    Dialog(
        modifier = modifier,
        dialogState = dialogState,
        onDismiss = onCancel,
        title = title,
        buttons = {
            DialogPositiveButton(text = confirmText, onClick = { onConfirm.invoke(timeVal) })
            DialogNegativeButton(text = cancelText, onClick = onCancel)
        },
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            ProvideTextStyle(value = MaterialTheme.typography.body1) {
                ProvideContentColor(color = ExtendedTheme.colors.text) {
                    content()
                }
            }
            TimePicker(
                currentTime = timeVal,
                onTimeChanged = {
                    timeVal = it
                    onValueChange?.invoke(it)
                },
                is24TimeFormat = is24TimeFormat,
                modifier = Modifier.height(150.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AlertDialog(
    dialogState: DialogState,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    confirmText: String = stringResource(id = R.string.button_ok),
    title: @Composable (DialogScope.() -> Unit) = {},
    content: @Composable (DialogScope.() -> Unit) = {},
) {
    Dialog(
        modifier = modifier,
        dialogState = dialogState,
        onDismiss = onDismiss,
        title = title,
        buttons = {
            DialogPositiveButton(text = confirmText)
        },
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            ProvideTextStyle(value = MaterialTheme.typography.body1) {
                ProvideContentColor(color = ExtendedTheme.colors.text) {
                    content()
                }
            }
        }
    }
}

@Composable
fun AlertDialog(
    dialogState: DialogState,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    title: @Composable (DialogScope.() -> Unit) = {},
    content: @Composable (DialogScope.() -> Unit) = {},
    buttons: @Composable (DialogScope.() -> Unit) = {},
) {
    Dialog(
        modifier = modifier,
        dialogState = dialogState,
        onDismiss = onDismiss,
        title = title,
        buttons = buttons,
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            ProvideTextStyle(value = MaterialTheme.typography.body1) {
                ProvideContentColor(color = ExtendedTheme.colors.text) {
                    content()
                }
            }
        }
    }
}

@Composable
fun ConfirmDialog(
    dialogState: DialogState,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    onCancel: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    confirmText: String = stringResource(id = R.string.button_sure_default),
    cancelText: String = stringResource(id = R.string.button_cancel),
    title: @Composable (DialogScope.() -> Unit)? = null,
    content: @Composable (DialogScope.() -> Unit) = {},
) {
    Dialog(
        modifier = modifier,
        dialogState = dialogState,
        onDismiss = onDismiss,
        title = title,
        buttons = {
            DialogPositiveButton(text = confirmText, onClick = onConfirm)
            DialogNegativeButton(text = cancelText, onClick = onCancel)
        },
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            ProvideTextStyle(value = MaterialTheme.typography.body1) {
                ProvideContentColor(color = ExtendedTheme.colors.text) {
                    content()
                }
            }
        }
    }
}

/**
 * 带输入框的对话框
 *
 * @param onValueChange 输入框内容变化时的回调，返回true表示允许变化，false表示不允许变化
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PromptDialog(
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier,
    dialogState: DialogState = rememberDialogState(),
    initialValue: String = "",
    onValueChange: (newVal: String, oldVal: String) -> Boolean = { _, _ -> true },
    onCancel: (() -> Unit)? = null,
    confirmText: String = stringResource(id = R.string.button_sure_default),
    cancelText: String = stringResource(id = R.string.button_cancel),
    title: @Composable (DialogScope.() -> Unit)? = null,
    content: @Composable (DialogScope.() -> Unit) = {},
) {
    var textVal by remember { mutableStateOf(initialValue) }
    // 每次显示时重置输入框内容
    LaunchedEffect(dialogState.show) {
        textVal = initialValue
    }
    Dialog(
        modifier = modifier,
        dialogState = dialogState,
        onDismiss = onCancel,
        title = title,
        buttons = {
            DialogPositiveButton(text = confirmText, onClick = { onConfirm.invoke(textVal) })
            DialogNegativeButton(text = cancelText, onClick = onCancel)
        },
    ) {
        val focusRequester = remember { FocusRequester() }
        val softwareKeyboardController = LocalSoftwareKeyboardController.current
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProvideTextStyle(value = MaterialTheme.typography.body1) {
                ProvideContentColor(color = ExtendedTheme.colors.text) {
                    content()
                }
            }
            OutlinedTextField(
                value = textVal,
                onValueChange = {
                    if (onValueChange.invoke(it, textVal)) textVal = it
                },
                maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { softwareKeyboardController?.hide() }
                ),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    cursorColor = ExtendedTheme.colors.primary,
                    focusedBorderColor = ExtendedTheme.colors.primary,
                    focusedLabelColor = ExtendedTheme.colors.primary
                )
            )
            LaunchedEffect(focusRequester) {
                focusRequester.requestFocus()
                launch {
                    delay(300)
                    softwareKeyboardController?.show()
                }
            }
        }
    }
}

@Composable
fun Dialog(
    modifier: Modifier = Modifier,
    dialogState: DialogState = rememberDialogState(),
    onDismiss: (() -> Unit)? = null,
    title: @Composable (DialogScope.() -> Unit)? = null,
    cancelable: Boolean = true,
    cancelableOnTouchOutside: Boolean = true,
    buttons: @Composable (DialogScope.() -> Unit) = {},
    content: @Composable (DialogScope.() -> Unit),
) {
    var showDialog by remember {
        mutableStateOf(false)
    }
    var isActiveClose by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(dialogState.show) {
        if (dialogState.show) {
            showDialog = true
            isActiveClose = false
        } else {
            isActiveClose = true
        }
    }
    if (showDialog) {
        val dialogScope = DialogScope(
            onDismiss = {
                isActiveClose = true
            },
        )
        AnyPopDialog(
            isActiveClose = isActiveClose,
            onDismiss = {
                onDismiss?.invoke()
                dialogState.show = false
                showDialog = false
            },
            properties = AnyPopDialogProperties(
                dismissOnBackPress = cancelable,
                dismissOnClickOutside = cancelableOnTouchOutside
            )
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(
                        color = ExtendedTheme.colors.windowBackground,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProvideContentColor(color = ExtendedTheme.colors.text) {
                    if (title != null) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            ProvideTextStyle(value = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)) {
                                dialogScope.title()
                            }
                        }
                    }
                    Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        dialogScope.content()
                    }
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dialogScope.buttons()
                    }
                }
            }
        }
    }
}

@Composable
fun rememberDialogState(): DialogState {
    return rememberSaveable(saver = DialogState.Saver) {
        DialogState()
    }
}

@Stable
class DialogState private constructor(
    show: Boolean
) {
    constructor() : this(show = false)

    var show by mutableStateOf(show)

    fun show() {
        show = true
    }

    companion object {
        val Saver: Saver<DialogState, *> = listSaver(
            save = {
                listOf<Any>(
                    it.show,
                )
            },
            restore = {
                DialogState(
                    show = it[0] as Boolean
                )
            }
        )
    }
}

class DialogScope(
    private val onDismiss: () -> Unit,
) {
    fun dismiss() {
        onDismiss()
    }
}