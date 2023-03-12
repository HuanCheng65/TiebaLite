package com.huanchengfly.tieba.post.ui.page.settings.oksign

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.BrowseGallery
import androidx.compose.material.icons.outlined.OfflinePin
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.dataStore
import com.huanchengfly.tieba.post.ui.common.prefs.PrefsScreen
import com.huanchengfly.tieba.post.ui.common.prefs.depend
import com.huanchengfly.tieba.post.ui.common.prefs.widgets.SwitchPref
import com.huanchengfly.tieba.post.ui.common.prefs.widgets.TextPref
import com.huanchengfly.tieba.post.ui.common.prefs.widgets.TimePickerPerf
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.page.settings.LeadingIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.AvatarIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.LocalSnackbarHostState
import com.huanchengfly.tieba.post.ui.widgets.compose.MyScaffold
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.ui.widgets.compose.TitleCentredToolbar
import com.huanchengfly.tieba.post.utils.isIgnoringBatteryOptimizations
import com.huanchengfly.tieba.post.utils.powerManager
import com.huanchengfly.tieba.post.utils.requestIgnoreBatteryOptimizations
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Destination
@Composable
fun OKSignSettingsPage(
    navigator: DestinationsNavigator,
) {
    val coroutineScope = rememberCoroutineScope()
    MyScaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            TitleCentredToolbar(
                title = stringResource(id = R.string.title_oksign),
                navigationIcon = {
                    BackNavigationIcon(onBackPressed = { navigator.navigateUp() })
                }
            )
        },
    ) { paddingValues ->
        val context = LocalContext.current
        val dataStore = context.dataStore
        val snackbarHostState = LocalSnackbarHostState.current
        PrefsScreen(
            dataStore = dataStore,
            dividerThickness = 0.dp,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            prefsItem {
                SwitchPref(
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.BrowseGallery,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    key = "oksign_slow_mode",
                    title = stringResource(id = R.string.title_oksign_slow_mode),
                    defaultChecked = true,
                    summaryOn = stringResource(id = R.string.summary_oksign_slow_mode_on),
                    summaryOff = stringResource(id = R.string.summary_oksign_slow_mode),
                )
            }
            prefsItem {
                SwitchPref(
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.Speed,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    key = "oksign_use_official_oksign",
                    title = stringResource(id = R.string.title_oksign_use_official_oksign),
                    defaultChecked = false,
                    summary = stringResource(id = R.string.summary_oksign_use_official_oksign),
                )
            }
            prefsItem {
                SwitchPref(
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.OfflinePin,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    key = "auto_sign",
                    title = stringResource(id = R.string.title_auto_sign),
                    defaultChecked = false,
                    summaryOn = stringResource(id = R.string.summary_auto_sign_on),
                    summaryOff = stringResource(id = R.string.summary_auto_sign),
                )
            }
            prefsItem {
                TimePickerPerf(
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.WatchLater,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    key = "auto_sign_time",
                    title = stringResource(id = R.string.title_auto_sign_time),
                    defaultValue = "09:00",
                    summary = { stringResource(id = R.string.summary_auto_sign_time, it) },
                    dialogTitle = stringResource(id = R.string.title_auto_sign_time),
                    enabled = depend(key = "auto_sign")
                )
            }
            prefsItem {
                TextPref(
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.BatteryAlert,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    title = stringResource(id = R.string.title_ignore_battery_optimization),
                    enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !context.isIgnoringBatteryOptimizations(),
                    summary =
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) stringResource(id = R.string.summary_battery_optimization_old_android_version)
                    else if (context.isIgnoringBatteryOptimizations()) stringResource(id = R.string.summary_battery_optimization_ignored)
                    else stringResource(id = R.string.summary_ignore_battery_optimization),
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!context.powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
                                context.requestIgnoreBatteryOptimizations()
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(context.getString(R.string.toast_ignore_battery_optimization_already))
                                }
                            }
                        }
                    }
                )
            }

            prefsItem {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(stringResource(id = R.string.tip_start))
                        }
                        append(stringResource(id = R.string.tip_auto_sign))
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .padding(start = 8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(color = ExtendedTheme.colors.chip)
                        .padding(12.dp),
                    color = ExtendedTheme.colors.onChip,
                    fontSize = 12.sp
                )
            }
        }
    }
}