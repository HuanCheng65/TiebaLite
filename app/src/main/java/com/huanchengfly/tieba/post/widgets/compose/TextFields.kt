package com.huanchengfly.tieba.post.widgets.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme

@Composable
fun BaseTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    placeholder: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: TextFieldColors = TextFieldDefaults.textFieldColors(),
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit =
        { innerTextField ->
            PlaceholderDecoration(
                show = value.isEmpty(),
                placeholderColor = colors.placeholderColor(enabled = enabled).value,
                placeholder = placeholder
            )
            innerTextField()
        }
) {
    val textColor = textStyle.color.takeOrElse {
        colors.textColor(enabled).value
    }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.background(color = colors.backgroundColor(enabled = enabled).value),
        enabled = enabled,
        readOnly = readOnly,
        textStyle = mergedTextStyle,
        cursorBrush = SolidColor(colors.cursorColor().value),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        singleLine = singleLine,
        maxLines = maxLines,
        decorationBox = decorationBox
    )
}

@Composable
fun CounterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxLength: Int = Int.MAX_VALUE,
    countWhitespace: Boolean = true,
    onLengthBeyondRestrict: ((String) -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    placeholder: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: CounterTextFieldColors = TextFieldDefaults.counterTextFieldColors(),
) {
    val maxLengthRestrictEnable = maxLength < Int.MAX_VALUE
    BaseTextField(
        value = value,
        onValueChange = {
            val count = it.count(countWhitespace)
            if (!maxLengthRestrictEnable || count <= maxLength) {
                onValueChange(it)
            } else {
                onValueChange(it.substring(0 until maxLength))
                onLengthBeyondRestrict?.invoke(it)
            }
        },
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        singleLine = singleLine,
        maxLines = maxLines,
        colors = colors,
        decorationBox = { innerTextField ->
            ConstraintLayout {
                val (innerTextFieldBox, counter) = createRefs()

                Box(
                    modifier = Modifier
                        .constrainAs(innerTextFieldBox) {
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            top.linkTo(parent.top)
                            width = Dimension.fillToConstraints
                        }
                ) {
                    PlaceholderDecoration(
                        show = value.isEmpty(),
                        placeholderColor = colors.placeholderColor(enabled = enabled).value,
                        placeholder = placeholder
                    )
                    innerTextField()
                }
                Text(
                    text = "${value.count(countWhitespace)}${if (maxLengthRestrictEnable) "/$maxLength" else ""}",
                    color = colors.counterColor(enabled = enabled).value,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .constrainAs(counter) {
                            top.linkTo(innerTextFieldBox.bottom, 8.dp)
                            end.linkTo(parent.end, 4.dp)
                            bottom.linkTo(parent.bottom)
                        },
                )
            }
        }
    )
}

private fun String.count(countWhitespace: Boolean = true): Int {
    return count { !it.isWhitespace() || countWhitespace }
}

@Composable
fun PlaceholderDecoration(
    show: Boolean,
    placeholderColor: Color,
    placeholder: @Composable (() -> Unit)? = null,
) {
    if (placeholder != null && show) {
        ProvideContentColor(color = placeholderColor) {
            placeholder()
        }
    }
}

@Composable
fun ProvideContentColor(
    color: Color,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalContentColor provides color) {
        content()
    }
}

@Stable
interface TextFieldColors {
    @Composable
    fun textColor(enabled: Boolean): State<Color>

    @Composable
    fun backgroundColor(enabled: Boolean): State<Color>

    @Composable
    fun placeholderColor(enabled: Boolean): State<Color>

    @Composable
    fun cursorColor(): State<Color>
}

@Stable
interface CounterTextFieldColors : TextFieldColors {
    @Composable
    fun counterColor(enabled: Boolean): State<Color>
}

@Immutable
private open class DefaultTextFieldColors(
    private val textColor: Color,
    private val disabledTextColor: Color,
    private val cursorColor: Color,
    private val backgroundColor: Color,
    private val placeholderColor: Color,
    private val disabledPlaceholderColor: Color,
) : TextFieldColors {
    @Composable
    override fun textColor(enabled: Boolean): State<Color> =
        rememberUpdatedState(newValue = if (enabled) textColor else disabledTextColor)

    @Composable
    override fun backgroundColor(enabled: Boolean): State<Color> =
        rememberUpdatedState(newValue = backgroundColor)

    @Composable
    override fun placeholderColor(enabled: Boolean): State<Color> =
        rememberUpdatedState(newValue = if (enabled) placeholderColor else disabledPlaceholderColor)

    @Composable
    override fun cursorColor(): State<Color> =
        rememberUpdatedState(newValue = cursorColor)

}

@Immutable
private class DefaultCounterTextFieldColors(
    textColor: Color,
    disabledTextColor: Color,
    cursorColor: Color,
    backgroundColor: Color,
    placeholderColor: Color,
    disabledPlaceholderColor: Color,
    private val counterColor: Color,
    private val disabledCounterColor: Color
) : DefaultTextFieldColors(
    textColor = textColor,
    disabledTextColor = disabledTextColor,
    cursorColor = cursorColor,
    backgroundColor = backgroundColor,
    placeholderColor = placeholderColor,
    disabledPlaceholderColor = disabledPlaceholderColor,
), CounterTextFieldColors {
    @Composable
    override fun counterColor(enabled: Boolean): State<Color> =
        rememberUpdatedState(newValue = if (enabled) counterColor else disabledCounterColor)
}

object TextFieldDefaults {
    @Composable
    fun textFieldColors(
        textColor: Color = ExtendedTheme.colors.text,
        disabledTextColor: Color = textColor.copy(alpha = ContentAlpha.disabled),
        backgroundColor: Color = Color.Transparent,
        cursorColor: Color = ExtendedTheme.colors.primary,
        placeholderColor: Color = ExtendedTheme.colors.text.copy(alpha = ContentAlpha.medium),
        disabledPlaceholderColor: Color = placeholderColor.copy(alpha = ContentAlpha.disabled),
    ): TextFieldColors =
        DefaultTextFieldColors(
            textColor = textColor,
            disabledTextColor = disabledTextColor,
            cursorColor = cursorColor,
            backgroundColor = backgroundColor,
            placeholderColor = placeholderColor,
            disabledPlaceholderColor = disabledPlaceholderColor,
        )

    @Composable
    fun counterTextFieldColors(
        textColor: Color = ExtendedTheme.colors.text,
        disabledTextColor: Color = textColor.copy(alpha = ContentAlpha.disabled),
        backgroundColor: Color = Color.Transparent,
        cursorColor: Color = ExtendedTheme.colors.primary,
        placeholderColor: Color = ExtendedTheme.colors.text.copy(alpha = ContentAlpha.medium),
        disabledPlaceholderColor: Color = placeholderColor.copy(alpha = ContentAlpha.disabled),
        counterColor: Color = ExtendedTheme.colors.text.copy(alpha = ContentAlpha.medium),
        disabledCounterColor: Color = counterColor.copy(alpha = ContentAlpha.disabled)
    ): CounterTextFieldColors =
        DefaultCounterTextFieldColors(
            textColor = textColor,
            disabledTextColor = disabledTextColor,
            cursorColor = cursorColor,
            backgroundColor = backgroundColor,
            placeholderColor = placeholderColor,
            disabledPlaceholderColor = disabledPlaceholderColor,
            counterColor = counterColor,
            disabledCounterColor = disabledCounterColor,
        )
}