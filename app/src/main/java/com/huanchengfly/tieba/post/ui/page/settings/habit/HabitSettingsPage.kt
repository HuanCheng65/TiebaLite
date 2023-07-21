package com.huanchengfly.tieba.post.ui.page.settings.habit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrandingWatermark
import androidx.compose.material.icons.outlined.CalendarViewDay
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.PhotoSizeSelectActual
import androidx.compose.material.icons.outlined.SecurityUpdateWarning
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.dataStore
import com.huanchengfly.tieba.post.ui.common.prefs.PrefsScreen
import com.huanchengfly.tieba.post.ui.common.prefs.widgets.ListPref
import com.huanchengfly.tieba.post.ui.common.prefs.widgets.SwitchPref
import com.huanchengfly.tieba.post.ui.page.settings.LeadingIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.AvatarIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.MyScaffold
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.ui.widgets.compose.TitleCentredToolbar
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Destination
@Composable
fun HabitSettingsPage(
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    MyScaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            TitleCentredToolbar(
                title = stringResource(id = R.string.title_settings_read_habit),
                navigationIcon = {
                    BackNavigationIcon(onBackPressed = { navigator.navigateUp() })
                }
            )
        },
    ) { paddingValues ->
        PrefsScreen(
            dataStore = LocalContext.current.dataStore,
            dividerThickness = 0.dp,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            prefsItem {
                ListPref(
                    key = "image_load_type",
                    title = stringResource(id = R.string.title_settings_image_load_type),
                    entries = mapOf(
                        "0" to stringResource(id = R.string.title_image_load_type_smart_origin),
                        "1" to stringResource(id = R.string.title_image_load_type_smart_load),
                        "2" to stringResource(id = R.string.title_image_load_type_all_origin),
                        "3" to stringResource(id = R.string.title_image_load_type_all_no)
                    ),
                    useSelectedAsSummary = true,
                    defaultValue = "0",
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.PhotoSizeSelectActual,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                )
            }
            prefsItem {
                ListPref(
                    key = "pic_watermark_type",
                    title = stringResource(id = R.string.title_settings_image_watermark),
                    entries = mapOf(
                        "0" to stringResource(id = R.string.title_image_watermark_none),
                        "1" to stringResource(id = R.string.title_image_watermark_user_name),
                        "2" to stringResource(id = R.string.title_image_watermark_forum_name)
                    ),
                    useSelectedAsSummary = true,
                    defaultValue = "2",
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.BrandingWatermark,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                )
            }
            prefsItem {
                ListPref(
                    key = "default_sort_type",
                    title = stringResource(id = R.string.title_settings_default_sort_type),
                    entries = mapOf(
                        "0" to stringResource(id = R.string.title_sort_by_reply),
                        "1" to stringResource(id = R.string.title_sort_by_send),
                    ),
                    useSelectedAsSummary = true,
                    defaultValue = "0",
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.CalendarViewDay,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                )
            }
            prefsItem {
                ListPref(
                    key = "forumFabFunction",
                    title = stringResource(id = R.string.settings_forum_fab_function),
                    defaultValue = "post",
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.ExitToApp,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    useSelectedAsSummary = true,
                    entries = mapOf(
                        "post" to stringResource(id = R.string.btn_post),
                        "refresh" to stringResource(id = R.string.btn_refresh),
                        "back_to_top" to stringResource(id = R.string.btn_back_to_top)
                    )
                )
            }
            prefsItem {
                SwitchPref(
                    key = "showShortcutInThread",
                    title = stringResource(id = R.string.settings_show_shortcut),
                    defaultChecked = true,
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = ImageVector.vectorResource(id = R.drawable.ic_quick_yellow),
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    summaryOn = stringResource(id = R.string.tip_show_shortcut_in_thread_on),
                    summaryOff = stringResource(id = R.string.tip_show_shortcut_in_thread)
                )
            }
            prefsItem {
                SwitchPref(
                    key = "collect_thread_see_lz",
                    title = stringResource(id = R.string.settings_collect_thread_see_lz),
                    defaultChecked = true,
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.StarOutline,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    summaryOn = stringResource(id = R.string.tip_collect_thread_see_lz_on),
                    summaryOff = stringResource(id = R.string.tip_collect_thread_see_lz)
                )
            }
            prefsItem {
                SwitchPref(
                    key = "show_both_username_and_nickname",
                    title = stringResource(id = R.string.title_show_both_username_and_nickname),
                    defaultChecked = false,
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.Verified,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                )
            }
            prefsItem {
                SwitchPref(
                    key = "postOrReplyWarning",
                    title = stringResource(id = R.string.title_post_or_reply_warning),
                    defaultChecked = true,
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.SecurityUpdateWarning,
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