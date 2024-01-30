package com.huanchengfly.tieba.post.ui.page.settings.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.SupervisedUserCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.dataStore
import com.huanchengfly.tieba.post.ui.common.prefs.PrefsScreen
import com.huanchengfly.tieba.post.ui.common.prefs.widgets.DropDownPref
import com.huanchengfly.tieba.post.ui.common.prefs.widgets.EditTextPref
import com.huanchengfly.tieba.post.ui.common.prefs.widgets.TextPref
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.page.destinations.LoginPageDestination
import com.huanchengfly.tieba.post.ui.page.settings.LeadingIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.AvatarIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.ui.widgets.compose.TitleCentredToolbar
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.AccountUtil.AllAccounts
import com.huanchengfly.tieba.post.utils.AccountUtil.LocalAccount
import com.huanchengfly.tieba.post.utils.TiebaUtil
import com.huanchengfly.tieba.post.utils.appPreferences
import com.huanchengfly.tieba.post.utils.launchUrl
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Destination
@Composable
fun AccountManagePage(
    navigator: DestinationsNavigator,
) {
    Scaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            TitleCentredToolbar(
                title = {
                    Text(
                        text = stringResource(id = R.string.title_account_manage),
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.h6
                    )
                },
                navigationIcon = {
                    BackNavigationIcon(onBackPressed = { navigator.navigateUp() })
                }
            )
        },
    ) { paddingValues ->
        val account = LocalAccount.current
        val context = LocalContext.current
        PrefsScreen(
            dataStore = LocalContext.current.dataStore,
            dividerThickness = 0.dp,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            prefsItem {
                if (account != null) {
                    DropDownPref(
                        key = "switch_account",
                        title = stringResource(id = R.string.title_switch_account),
                        summary = stringResource(
                            id = R.string.summary_now_account,
                            account.nameShow ?: account.name
                        ),
                        leadingIcon = {
                            LeadingIcon {
                                AvatarIcon(
                                    icon = Icons.Outlined.AccountCircle,
                                    size = Sizes.Small,
                                    contentDescription = null,
                                )
                            }
                        },
                        onValueChange = { AccountUtil.switchAccount(context, it.toInt()) },
                        enabled = true,
                        defaultValue = account.id.toString(),
                        entries = AllAccounts.current.associate {
                            it.id.toString() to (it.nameShow ?: it.name)
                        }
                    )
                } else {
                    TextPref(
                        title = stringResource(id = R.string.title_switch_account),
                        summary = null,
                        leadingIcon = {
                            LeadingIcon {
                                AvatarIcon(
                                    icon = Icons.Outlined.AccountCircle,
                                    size = Sizes.Small,
                                    contentDescription = null,
                                )
                            }
                        },
                        darkenOnDisable = true,
                        enabled = false,
                    )
                }
            }
            prefsItem {
                TextPref(
                    title = stringResource(id = R.string.title_new_account),
                    onClick = { navigator.navigate(LoginPageDestination) },
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.AddCircleOutline,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
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
                        append(stringResource(id = R.string.tip_account_error))
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
            prefsItem {
                TextPref(
                    title = stringResource(id = R.string.title_exit_account),
                    onClick = { AccountUtil.exit(context) },
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = ImageVector.vectorResource(id = R.drawable.ic_outlined_close_circle_24),
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                )
            }
            prefsItem {
                TextPref(
                    title = stringResource(id = R.string.title_modify_username),
                    onClick = {
                        launchUrl(
                            context,
                            navigator,
                            "https://wappass.baidu.com/static/manage-chunk/change-username.html#/showUsername"
                        )
                    },
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.SupervisedUserCircle,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    enabled = account != null
                )
            }
            prefsItem {
                TextPref(
                    title = stringResource(id = R.string.title_copy_bduss),
                    summary = stringResource(id = R.string.summary_copy_bduss),
                    onClick = { TiebaUtil.copyText(context, account?.bduss, isSensitive = true) },
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.ContentCopy,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    enabled = account != null
                )
            }
            prefsItem {
                val littleTail = remember { context.appPreferences.littleTail }
                EditTextPref(
                    key = "little_tail",
                    title = stringResource(id = R.string.title_my_tail),
                    summary = if (littleTail.isNullOrEmpty())
                        stringResource(id = R.string.tip_no_little_tail)
                    else
                        littleTail,
                    leadingIcon = {
                        LeadingIcon {
                            AvatarIcon(
                                icon = Icons.Outlined.Edit,
                                size = Sizes.Small,
                                contentDescription = null,
                            )
                        }
                    },
                    enabled = true,
                    dialogTitle = stringResource(id = R.string.title_dialog_modify_little_tail),
                )
            }
        }
    }
}