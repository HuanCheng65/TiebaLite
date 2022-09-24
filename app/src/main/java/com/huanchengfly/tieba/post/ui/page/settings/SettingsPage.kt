package com.huanchengfly.tieba.post.ui.page.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.ui.Scaffold
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.LoginActivity
import com.huanchengfly.tieba.post.dataStore
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.models.database.Account
import com.huanchengfly.tieba.post.ui.common.prefs.PrefsScreen
import com.huanchengfly.tieba.post.ui.common.prefs.widgets.TextPref
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.ProvideNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.*
import com.huanchengfly.tieba.post.ui.widgets.compose.*
import com.huanchengfly.tieba.post.utils.AccountUtil.LocalAccount
import com.huanchengfly.tieba.post.utils.StringUtil
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Composable
internal fun LeadingIcon(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalContentColor provides ExtendedTheme.colors.accent) {
        content()
        Spacer(modifier = Modifier.width(56.dp))
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NowAccountItem(
    account: Account?,
    modifier: Modifier = Modifier
) {
    if (account != null) {
        val navigator = LocalNavigator.current
        TextPref(
            title = stringResource(id = R.string.title_account_manage),
            summary = stringResource(id = R.string.summary_now_account, account.nameShow ?: account.name),
            enabled = true,
            onClick = { navigator.navigate(AccountManagePageDestination) },
            leadingIcon = {
                LeadingIcon {
                    Avatar(
                        data = StringUtil.getAvatarUrl(account.portrait),
                        size = Sizes.Small,
                        contentDescription = null
                    )
                }
            },
            modifier = modifier,
        )
    } else {
        val context = LocalContext.current
        TextPref(
            title = stringResource(id = R.string.title_account_manage),
            summary = stringResource(id = R.string.summary_not_logged_in),
            enabled = true,
            onClick = { context.goToActivity<LoginActivity>() },
            leadingIcon = {
                LeadingIcon {
                    AvatarIcon(
                        icon = Icons.Rounded.AccountCircle,
                        size = Sizes.Small,
                        contentDescription = stringResource(id = R.string.title_new_account),
                        color = ExtendedTheme.colors.onChip,
                        backgroundColor = ExtendedTheme.colors.chip,
                    )
                }
            },
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Destination
@Composable
fun SettingsPage(
    navigator: DestinationsNavigator,
) {
    Scaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            TitleCentredToolbar(
                title = stringResource(id = R.string.title_settings),
                navigationIcon = {
                    BackNavigationIcon(onBackPressed = { navigator.navigateUp() })
                }
            )
        },
    ) {
        ProvideNavigator(navigator = navigator) {
            PrefsScreen(
                dataStore = LocalContext.current.dataStore,
                dividerThickness = 0.dp,
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize(),
            ) {
                prefsItem {
                    NowAccountItem(account = LocalAccount.current)
                }
                prefsItem {
                    TextPref(
                        title = stringResource(id = R.string.title_block_settings),
                        summary = stringResource(id = R.string.summary_account_manage),
                        leadingIcon = {
                            LeadingIcon {
                                AvatarIcon(
                                    icon = ImageVector.vectorResource(id = R.drawable.ic_settings_block),
                                    contentDescription = null,
                                    size = Sizes.Small,
                                )
                            }
                        },
                        darkenOnDisable = false,
                        onClick = { navigator.navigate(BlockSettingsPageDestination) }
                    )
                }
                prefsItem {
                    TextPref(
                        title = stringResource(id = R.string.title_settings_custom),
                        summary = stringResource(id = R.string.summary_settings_custom),
                        leadingIcon = {
                            LeadingIcon {
                                AvatarIcon(
                                    icon = ImageVector.vectorResource(id = R.drawable.ic_brush_black_24dp),
                                    contentDescription = null,
                                    size = Sizes.Small,
                                )
                            }
                        },
                        darkenOnDisable = false,
                        onClick = { navigator.navigate(CustomSettingsPageDestination) }
                    )
                }
                prefsItem {
                    TextPref(
                        title = stringResource(id = R.string.title_settings_read_habit),
                        summary = stringResource(id = R.string.summary_settings_habit),
                        leadingIcon = {
                            LeadingIcon {
                                AvatarIcon(
                                    icon = ImageVector.vectorResource(id = R.drawable.ic_dashboard_customize_black_24),
                                    contentDescription = null,
                                    size = Sizes.Small,
                                )
                            }
                        },
                        darkenOnDisable = false,
                        onClick = { navigator.navigate(HabitSettingsPageDestination) }
                    )
                }
                prefsItem {
                    TextPref(
                        title = stringResource(id = R.string.title_oksign),
                        summary = stringResource(id = R.string.summary_settings_oksign),
                        leadingIcon = {
                            LeadingIcon {
                                AvatarIcon(
                                    icon = ImageVector.vectorResource(id = R.drawable.ic_rocket_launch_black_24),
                                    contentDescription = null,
                                    size = Sizes.Small,
                                )
                            }
                        },
                        darkenOnDisable = false,
                        onClick = {
                            navigator.navigate(OKSignSettingsPageDestination)
                        }
                    )
                }
                prefsItem {
                    TextPref(
                        title = stringResource(id = R.string.title_settings_more),
                        summary = stringResource(id = R.string.summary_settings_more),
                        leadingIcon = {
                            LeadingIcon {
                                AvatarIcon(
                                    icon = ImageVector.vectorResource(id = R.drawable.ic_more_horiz_black_24),
                                    contentDescription = null,
                                    size = Sizes.Small,
                                )
                            }
                        },
                        darkenOnDisable = false,
                        onClick = {
                            navigator.navigate(MoreSettingsPageDestination)
                        }
                    )
                }
            }
        }
    }
}