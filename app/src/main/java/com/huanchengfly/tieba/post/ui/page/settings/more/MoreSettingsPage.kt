package com.huanchengfly.tieba.post.ui.page.settings.more

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.FiberNew
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.OfflineBolt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.BuildConfig
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.dataStore
import com.huanchengfly.tieba.post.ui.common.prefs.PrefsScreen
import com.huanchengfly.tieba.post.ui.common.prefs.dependNot
import com.huanchengfly.tieba.post.ui.common.prefs.widgets.SwitchPref
import com.huanchengfly.tieba.post.ui.common.prefs.widgets.TextPref
import com.huanchengfly.tieba.post.ui.page.destinations.AboutPageDestination
import com.huanchengfly.tieba.post.ui.page.settings.LeadingIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.AvatarIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.LocalSnackbarHostState
import com.huanchengfly.tieba.post.ui.widgets.compose.MyScaffold
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.ui.widgets.compose.TitleCentredToolbar
import com.huanchengfly.tieba.post.utils.ImageCacheUtil
import com.huanchengfly.tieba.post.utils.appPreferences
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

@OptIn(ExperimentalMaterialApi::class)
@Destination
@Composable
fun MoreSettingsPage(
    navigator: DestinationsNavigator,
) {
    val coroutineScope = rememberCoroutineScope()
    MyScaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            TitleCentredToolbar(
                title = {
                    Text(
                        text = stringResource(id = R.string.title_settings_more),
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.h6
                    )
                },
                navigationIcon = {
                    BackNavigationIcon(onBackPressed = { navigator.navigateUp() })
                }
            )
        },
    ) { paddingValues ->
        val snackbarHostState = LocalSnackbarHostState.current
        val context = LocalContext.current
        var cacheSize by remember { mutableStateOf("0.0B") }
        LaunchedEffect(Unit) {
            thread {
                cacheSize = ImageCacheUtil.getCacheSize(context)
            }
        }
        PrefsScreen(
            dataStore = LocalContext.current.dataStore,
            dividerThickness = 0.dp,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            if (context.appPreferences.showExperimentalFeatures) {
                prefsItem {
                    SwitchPref(
                        leadingIcon = {
                            LeadingIcon {
                                AvatarIcon(
                                    icon = Icons.Outlined.BugReport,
                                    size = Sizes.Small,
                                    contentDescription = null,
                                )
                            }
                        },
                        key = "checkCIUpdate",
                        title = stringResource(id = R.string.title_check_ci_update),
                        defaultChecked = false,
                        summary = stringResource(id = R.string.tip_check_ci_update)
                    )
                }
            }
            prefsItem {
                SwitchPref(
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.FiberNew,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    key = "enableNewUi",
                    title = stringResource(id = R.string.title_enable_new_ui),
                    defaultChecked = false,
                    summary = stringResource(id = R.string.summary_enable_new_ui),
                    onCheckedChange = {
                        App.INSTANCE.setIcon(it)
                    }
                )
            }
            prefsItem {
                SwitchPref(
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = ImageVector.vectorResource(id = R.drawable.ic_chrome),
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    key = "use_webview",
                    title = stringResource(id = R.string.title_use_webview),
                    defaultChecked = true,
                    summary = {
                        if (it) stringResource(id = R.string.tip_use_webview_on)
                        else stringResource(id = R.string.tip_use_webview)
                    },
                )
            }
            prefsItem {
                SwitchPref(
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = ImageVector.vectorResource(id = R.drawable.ic_today),
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    enabled = dependNot(key = "use_webview"),
                    key = "use_custom_tabs",
                    title = stringResource(id = R.string.title_use_custom_tabs),
                    defaultChecked = true,
                    summary = {
                        if (it) stringResource(id = R.string.tip_use_custom_tab_on)
                        else stringResource(id = R.string.tip_use_custom_tab)
                    },
                )
            }
            prefsItem {
                TextPref(
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.OfflineBolt,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    enabled = true,
                    title = stringResource(id = R.string.title_clear_picture_cache),
                    onClick = {
                        coroutineScope.launch {
                            ImageCacheUtil.clearImageAllCache(context)
                            cacheSize = "0.0B"
                            snackbarHostState.showSnackbar(context.getString(R.string.toast_clear_picture_cache_success))
                        }
                    },
                    summary = stringResource(id = R.string.tip_cache, cacheSize)
                )
            }
            prefsItem {
                TextPref(
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.Info,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    enabled = true,
                    title = stringResource(id = R.string.title_about),
                    onClick = {
                        navigator.navigate(AboutPageDestination)
                    },
                    summary = stringResource(id = R.string.tip_about, BuildConfig.VERSION_NAME)
                )
            }
        }
    }
}