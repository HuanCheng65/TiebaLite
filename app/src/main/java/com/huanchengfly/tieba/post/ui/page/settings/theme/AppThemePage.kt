package com.huanchengfly.tieba.post.ui.page.settings.theme

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.PhotoSizeSelectActual
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.github.panpf.sketch.compose.AsyncImage
import com.github.panpf.sketch.fetch.newFileUri
import com.github.panpf.sketch.fetch.newResourceUri
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.TranslucentThemeActivity
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.rememberPreferenceAsState
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.compose.dynamicTonalPalette
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.MyScaffold
import com.huanchengfly.tieba.post.ui.widgets.compose.ProvideContentColor
import com.huanchengfly.tieba.post.ui.widgets.compose.TitleCentredToolbar
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.appPreferences
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

    MyScaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            TitleCentredToolbar(
                title = stringResource(id = R.string.title_theme),
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(all = 16.dp)
            ) {

            }
            Column {
                ProvideContentColor(color = ExtendedTheme.colors.background) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val tonalPalette = remember { dynamicTonalPalette(context) }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(100))
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
                                        contentDescription = null
                                    )
                                    Text(
                                        text = stringResource(id = R.string.title_dynamic_theme),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(100))
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
                                        imageVector = if (ThemeUtil.isTranslucentTheme(currentTheme)) {
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
                LazyRow(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    itemsIndexed(
                        items = themeValues.toList(),
                        key = { _, item -> item }
                    ) { index, item ->
                        val name = themeNames[index]
                        val backgroundColor = Color(
                            App.ThemeDelegate.getColorByAttr(
                                LocalContext.current,
                                R.attr.colorBackground,
                                item
                            )
                        )
                        val primaryColor = Color(
                            App.ThemeDelegate.getColorByAttr(
                                LocalContext.current,
                                R.attr.colorNewPrimary,
                                item
                            )
                        )
                        val accentColor = Color(
                            App.ThemeDelegate.getColorByAttr(
                                LocalContext.current,
                                R.attr.colorAccent,
                                item
                            )
                        )
                        val onAccentColor = Color(
                            App.ThemeDelegate.getColorByAttr(
                                LocalContext.current,
                                R.attr.colorOnAccent,
                                item
                            )
                        )
                        val onBackgroundColor = Color(
                            App.ThemeDelegate.getColorByAttr(
                                LocalContext.current,
                                R.attr.colorOnBackground,
                                item
                            )
                        )
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
    Surface(
        border = BorderStroke(12.dp, accentColor),
        color = primaryColor,
        contentColor = contentColor,
        shape = CircleShape,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(
                onClickLabel = themeName,
                role = Role.Button,
                onClick = onClick
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = stringResource(id = R.string.desc_checked),
                )
            } else if (ThemeUtil.isNightMode(themeValue)) {
                Icon(
                    imageVector = Icons.Rounded.NightsStay,
                    contentDescription = stringResource(id = R.string.desc_night_theme),
                )
            }
        }
    }
}