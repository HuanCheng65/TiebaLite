package com.huanchengfly.tieba.post.ui.page.settings.custom

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.AppFontSizeActivity
import com.huanchengfly.tieba.post.dataStore
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.ui.common.prefs.PrefsScreen
import com.huanchengfly.tieba.post.ui.common.prefs.widgets.ListPref
import com.huanchengfly.tieba.post.ui.common.prefs.widgets.SwitchPref
import com.huanchengfly.tieba.post.ui.common.prefs.widgets.TextPref
import com.huanchengfly.tieba.post.ui.page.settings.LeadingIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.*
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Destination
@Composable
fun CustomSettingsPage(
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    MyScaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            TitleCentredToolbar(
                title = stringResource(id = R.string.title_settings_custom),
                navigationIcon = {
                    BackNavigationIcon(onBackPressed = { navigator.navigateUp() })
                }
            )
        },
    ) { paddingValues ->
        PrefsScreen(
            dataStore = context.dataStore,
            dividerThickness = 0.dp,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            prefsItem {
                TextPref(
                    title = stringResource(id = R.string.title_custom_font_size),
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.FontDownload,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    onClick = {
                        context.goToActivity<AppFontSizeActivity>()
                    }
                )
            }
            prefsItem {
                ListPref(
                    key = "dark_theme",
                    title = stringResource(id = R.string.settings_night_mode),
                    defaultValue = ThemeUtil.THEME_AMOLED_DARK,
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.Brightness2,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    entries = mapOf(
                        ThemeUtil.THEME_GREY_DARK to "深邃灰",
                        ThemeUtil.THEME_AMOLED_DARK to "A屏黑"
                    ),
                    useSelectedAsSummary = true,
                )
            }
            prefsItem {
                SwitchPref(
                    key = "follow_system_night",
                    title = stringResource(id = R.string.title_settings_follow_system_night),
                    defaultChecked = true,
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.BrightnessAuto,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                )
            }
            prefsItem {
                SwitchPref(
                    key = "listSingle",
                    title = stringResource(id = R.string.settings_forum_single),
                    defaultChecked = false,
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.ViewAgenda,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                )
            }
            prefsItem {
                SwitchPref(
                    key = "hideExplore",
                    title = stringResource(id = R.string.title_hide_explore),
                    defaultChecked = false,
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.Explore,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                )
            }
        }
    }
}