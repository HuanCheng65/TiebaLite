package com.huanchengfly.tieba.post.ui.page.settings.theme

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BorderColor
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.PhotoSizeSelectActual
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.github.panpf.sketch.compose.AsyncImage
import com.github.panpf.sketch.fetch.newFileUri
import com.github.panpf.sketch.fetch.newResourceUri
import com.godaddy.android.colorpicker.HsvColor
import com.godaddy.android.colorpicker.harmony.ColorHarmonyMode
import com.godaddy.android.colorpicker.harmony.HarmonyColorPicker
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.TranslucentThemeActivity
import com.huanchengfly.tieba.post.components.dialogs.CustomThemeDialog
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.rememberPreferenceAsMutableState
import com.huanchengfly.tieba.post.rememberPreferenceAsState
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.compose.dynamicTonalPalette
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.Dialog
import com.huanchengfly.tieba.post.ui.widgets.compose.DialogNegativeButton
import com.huanchengfly.tieba.post.ui.widgets.compose.DialogPositiveButton
import com.huanchengfly.tieba.post.ui.widgets.compose.MyScaffold
import com.huanchengfly.tieba.post.ui.widgets.compose.ProvideContentColor
import com.huanchengfly.tieba.post.ui.widgets.compose.TitleCentredToolbar
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberDialogState
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.appPreferences
import com.huanchengfly.tieba.post.utils.extension.toHexString
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@Composable
fun AppThemePage(
    navigator: DestinationsNavigator,
) {
    val context = LocalContext.current
    val themeValues = stringArrayResource(id = R.array.theme_values)
    val themeNames = stringArrayResource(id = R.array.themeNames)
    val currentTheme by remember { ThemeUtil.themeState }
    val isDynamicTheme by rememberPreferenceAsState(
        key = booleanPreferencesKey("useDynamicColorTheme"),
        defaultValue = false
    )
    val customPrimaryColorDialogState = rememberDialogState()
    var customPrimaryColor by remember {
        mutableStateOf(
            Color(
                App.ThemeDelegate.getColorByAttr(
                    context,
                    R.attr.colorPrimary,
                    ThemeUtil.THEME_CUSTOM
                )
            )
        )
    }
    var customToolbarPrimaryColor by rememberPreferenceAsMutableState(
        key = booleanPreferencesKey(
            ThemeUtil.KEY_CUSTOM_TOOLBAR_PRIMARY_COLOR
        ),
        defaultValue = false
    )
    var customStatusBarFontDark by rememberPreferenceAsMutableState(
        key = booleanPreferencesKey(
            ThemeUtil.KEY_CUSTOM_STATUS_BAR_FONT_DARK
        ),
        defaultValue = false
    )

    Dialog(
        dialogState = customPrimaryColorDialogState,
        title = { Text(text = stringResource(id = R.string.title_custom_theme)) },
        content = {
            var useInput by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedContent(
                    targetState = useInput,
                    label = "",
                    modifier = Modifier
                        .wrapContentHeight()
                        .animateContentSize()
                ) { input ->
                    if (input) {
                        var inputHexColor by remember { mutableStateOf(customPrimaryColor.toHexString()) }
                        val lastValidColor by produceState(
                            initialValue = customPrimaryColor,
                            inputHexColor
                        ) {
                            if ("^#([0-9a-fA-F]{6})$".toRegex().matches(inputHexColor)) {
                                value = Color(inputHexColor.toColorInt())
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Spacer(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(lastValidColor)
                            )
                            OutlinedTextField(
                                value = inputHexColor,
                                onValueChange = {
                                    if ("^#([0-9a-fA-F]{0,6})$".toRegex().matches(it)) {
                                        inputHexColor = it
                                    }
                                },
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    cursorColor = ExtendedTheme.colors.primary,
                                    focusedBorderColor = ExtendedTheme.colors.primary,
                                    focusedLabelColor = ExtendedTheme.colors.primary
                                )
                            )
                            IconButton(
                                onClick = {
                                    customPrimaryColor = Color(inputHexColor.toColorInt())
                                    useInput = false
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = stringResource(id = R.string.button_sure_default)
                                )
                            }
                        }
                    } else {
                        Box {
                            HarmonyColorPicker(
                                harmonyMode = ColorHarmonyMode.ANALOGOUS,
                                color = HsvColor.from(customPrimaryColor),
                                onColorChanged = {
                                    customPrimaryColor = it.toColor()
                                },
                                modifier = Modifier.sizeIn(maxWidth = 320.dp, maxHeight = 320.dp)
                            )

                            IconButton(
                                onClick = { useInput = true },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.BorderColor,
                                    contentDescription = stringResource(id = R.string.desc_input_color)
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                customToolbarPrimaryColor =
                                    !customToolbarPrimaryColor
                            }
                        )
                ) {
                    Checkbox(
                        checked = customToolbarPrimaryColor,
                        onCheckedChange = {
                            customToolbarPrimaryColor = it
                        },
                    )
                    Text(text = stringResource(id = R.string.tip_toolbar_primary_color))
                }

                if (customToolbarPrimaryColor) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    customStatusBarFontDark =
                                        !customStatusBarFontDark
                                }
                            )
                    ) {
                        Checkbox(
                            checked = customStatusBarFontDark,
                            onCheckedChange = {
                                customStatusBarFontDark = it
                            },
                        )
                        Text(text = stringResource(id = R.string.tip_status_bar_font))
                    }
                }
            }
        },
        buttons = {
            DialogPositiveButton(
                text = stringResource(id = R.string.button_finish),
                onClick = {
                    customStatusBarFontDark = customStatusBarFontDark || !customToolbarPrimaryColor
                    context.appPreferences.customPrimaryColor =
                        CustomThemeDialog.toString(customPrimaryColor.toArgb())
                    context.appPreferences.toolbarPrimaryColor = customToolbarPrimaryColor
                    context.appPreferences.customStatusBarFontDark = customStatusBarFontDark
                    ThemeUtil.setUseDynamicTheme(false)
                    ThemeUtil.switchTheme(ThemeUtil.THEME_CUSTOM)
                }
            )
            DialogNegativeButton(
                text = stringResource(id = R.string.button_cancel),
                onClick = {
                    customPrimaryColor = Color(
                        App.ThemeDelegate.getColorByAttr(
                            context,
                            R.attr.colorPrimary,
                            ThemeUtil.THEME_CUSTOM
                        )
                    )
                }
            )
        }
    )

    MyScaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            TitleCentredToolbar(
                title = {
                    Text(
                        text = stringResource(id = R.string.title_theme),
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.h6
                    )
                },
                navigationIcon = {
                    BackNavigationIcon(onBackPressed = { navigator.navigateUp() })
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    item {
                        val tonalPalette = remember { dynamicTonalPalette(context) }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    brush = Brush.sweepGradient(
                                        colors = listOf(
                                            tonalPalette.primary50,
                                            tonalPalette.secondary50,
                                            tonalPalette.tertiary50,
                                            tonalPalette.primary50,
                                        )
                                    )
                                )
                                .clickable {
                                    ThemeUtil.setUseDynamicTheme(true)
                                }
                                .padding(all = 16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                Icon(
                                    imageVector = if (isDynamicTheme) {
                                        Icons.Rounded.Check
                                    } else {
                                        Icons.Rounded.Colorize
                                    },
                                    contentDescription = null,
                                    tint = ExtendedTheme.colors.windowBackground
                                )
                                Text(
                                    text = stringResource(id = R.string.title_dynamic_theme),
                                    fontWeight = FontWeight.Bold,
                                    color = ExtendedTheme.colors.windowBackground
                                )
                            }
                        }
                    }
                }
                item {
                    ProvideContentColor(color = ExtendedTheme.colors.windowBackground) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        color = customPrimaryColor,
                                    )
                                    .clickable {
                                        customPrimaryColorDialogState.show()
                                    }
                                    .padding(all = 16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
                                    Icon(
                                        imageVector = if (currentTheme == ThemeUtil.THEME_CUSTOM) {
                                            Icons.Rounded.Check
                                        } else {
                                            Icons.Rounded.ColorLens
                                        },
                                        contentDescription = null
                                    )
                                    Text(
                                        text = stringResource(id = R.string.title_custom_color),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        context.goToActivity<TranslucentThemeActivity>()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                val previewImageUri =
                                    if (context.appPreferences.translucentThemeBackgroundPath != null) {
                                        newFileUri(context.appPreferences.translucentThemeBackgroundPath!!)
                                    } else {
                                        newResourceUri(R.drawable.user_header)
                                    }
                                AsyncImage(
                                    imageUri = previewImageUri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(all = 16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    ) {
                                        Icon(
                                            imageVector = if (ThemeUtil.isTranslucentTheme(
                                                    currentTheme
                                                )
                                            ) {
                                                Icons.Rounded.Check
                                            } else {
                                                Icons.Rounded.PhotoSizeSelectActual
                                            },
                                            contentDescription = null
                                        )
                                        Text(
                                            text = stringResource(id = R.string.title_theme_translucent),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                itemsIndexed(
                    items = themeValues.toList(),
                    key = { _, item -> item }
                ) { index, item ->
                    val name = themeNames[index]
                    val backgroundColor = remember {
                        Color(
                            App.ThemeDelegate.getColorByAttr(
                                context,
                                R.attr.colorBackground,
                                item
                            )
                        )
                    }
                    val primaryColor = remember {
                        Color(
                            App.ThemeDelegate.getColorByAttr(
                                context,
                                R.attr.colorNewPrimary,
                                item
                            )
                        )
                    }
                    val accentColor = remember {
                        Color(
                            App.ThemeDelegate.getColorByAttr(
                                context,
                                R.attr.colorAccent,
                                item
                            )
                        )
                    }
                    val onAccentColor = remember {
                        Color(
                            App.ThemeDelegate.getColorByAttr(
                                context,
                                R.attr.colorOnAccent,
                                item
                            )
                        )
                    }
                    val onBackgroundColor = remember {
                        Color(
                            App.ThemeDelegate.getColorByAttr(
                                context,
                                R.attr.colorText,
                                item
                            )
                        )
                    }
                    if (index == 0) {
                        Spacer(modifier = Modifier.size(16.dp))
                    }
                    if (ThemeUtil.isNightMode(item)) {
                        ThemeItem(
                            themeName = name,
                            themeValue = item,
                            primaryColor = backgroundColor,
                            accentColor = backgroundColor,
                            contentColor = onBackgroundColor,
                            selected = !isDynamicTheme && currentTheme == item,
                            onClick = {
                                ThemeUtil.switchTheme(item)
                                ThemeUtil.setUseDynamicTheme(false)
                            }
                        )
                    } else {
                        ThemeItem(
                            themeName = name,
                            themeValue = item,
                            primaryColor = primaryColor,
                            accentColor = accentColor,
                            contentColor = onAccentColor,
                            selected = !isDynamicTheme && currentTheme == item,
                            onClick = {
                                ThemeUtil.switchTheme(item)
                                ThemeUtil.setUseDynamicTheme(false)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeItem(
    themeName: String,
    themeValue: String,
    primaryColor: Color,
    accentColor: Color,
    contentColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable(
                onClickLabel = themeName,
                onClick = onClick
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        listOf(
                            primaryColor,
                            accentColor,
                        )
                    )
                )
                .padding(9.dp),
        ) {
            if (ThemeUtil.isNightMode(themeValue)) {
                Icon(
                    imageVector = Icons.Rounded.NightsStay,
                    contentDescription = stringResource(id = R.string.desc_night_theme),
                    tint = contentColor
                )
            }
        }
        Text(
            text = themeName,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = stringResource(id = R.string.desc_checked),
                tint = ExtendedTheme.colors.primary
            )
        }
    }
}