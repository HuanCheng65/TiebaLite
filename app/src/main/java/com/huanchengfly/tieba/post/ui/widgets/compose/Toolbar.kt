package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.LoginActivity
import com.huanchengfly.tieba.post.arch.BaseComposeActivity.Companion.LocalWindowSizeClass
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.windowsizeclass.WindowWidthSizeClass.Companion.Compact
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.AccountUtil.LocalAccount
import com.huanchengfly.tieba.post.utils.StringUtil
import com.huanchengfly.tieba.post.utils.compose.calcStatusBarColor

@Composable
fun AccountNavIconIfCompact(): (@Composable () -> Unit)? =
    if (LocalWindowSizeClass.current.widthSizeClass == Compact) (@Composable { AccountNavIcon() })
    else null

@Composable
fun AccountNavIcon(
    onClick: (() -> Unit)? = null,
    spacer: Boolean = true,
    size: Dp = Sizes.Small
) {
    val currentAccount = LocalAccount.current
    if (spacer) Spacer(modifier = Modifier.width(12.dp))
    if (currentAccount == null) {
        Image(
            painter = rememberDrawablePainter(drawable = LocalContext.current.getDrawable(R.drawable.ic_launcher_new_round)),
            contentDescription = null,
            modifier = Modifier
                .clip(CircleShape)
                .size(size)
        )
    } else {
        val context = LocalContext.current
        val menuState = rememberMenuState()
        LongClickMenu(
            menuState = menuState,
            menuContent = {
                val allAccounts = AccountUtil.allAccounts
                allAccounts.forEach {
                    DropdownMenuItem(onClick = { AccountUtil.switchUser(context, it.id) }) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(Sizes.Small)
                        ) {
                            Avatar(
                                data = StringUtil.getAvatarUrl(it.portrait),
                                contentDescription = stringResource(id = R.string.title_switch_account_long_press),
                                size = Sizes.Small,
                            )
                            if (currentAccount.id == it.id) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = stringResource(id = R.string.desc_current_account),
                                    tint = Color.White,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color = Color.Black.copy(0.35f))
                                        .padding(8.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = it.nameShow ?: it.name)
                    }
                }
                VerticalDivider(
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                DropdownMenuItem(onClick = {
                    context.goToActivity<LoginActivity>()
                }) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(id = R.string.title_new_account),
                        tint = ExtendedTheme.colors.onChip,
                        modifier = Modifier
                            .size(Sizes.Small)
                            .clip(CircleShape)
                            .background(color = ExtendedTheme.colors.chip)
                            .padding(8.dp),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = stringResource(id = R.string.title_new_account))
                }
            },
            onClick = onClick,
            shape = CircleShape
        ) {
            Avatar(
                data = StringUtil.getAvatarUrl(currentAccount.portrait),
                size = size,
                contentDescription = stringResource(id = R.string.title_switch_account_long_press)
            )
        }
    }
}

@Composable
fun ActionItem(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    ProvideContentColor(color = ExtendedTheme.colors.onTopBar) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription
            )
        }
    }
}

@Composable
fun BackNavigationIcon(onBackPressed: () -> Unit) {
    ProvideContentColor(color = ExtendedTheme.colors.onTopBar) {
        IconButton(onClick = onBackPressed) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_round_arrow_back),
                contentDescription = stringResource(id = R.string.button_back)
            )
        }
    }
}

@Composable
fun TitleCentredToolbar(
    title: String,
    insets: Boolean = true,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val statusBarModifier = if (insets) {
        Modifier.windowInsetsTopHeight(WindowInsets.statusBars)
    } else {
        Modifier
    }
    Column {
        Spacer(
            modifier = statusBarModifier
                .fillMaxWidth()
                .background(color = ExtendedTheme.colors.topBar.calcStatusBarColor())
        )
        TopAppBar(
            backgroundColor = ExtendedTheme.colors.topBar,
            contentColor = ExtendedTheme.colors.onTopBar,
            elevation = 0.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    navigationIcon?.invoke()

                    Spacer(modifier = Modifier.weight(1f))

                    actions()
                }

                Row(
                    Modifier
                        .fillMaxHeight()
                        .align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    ProvideTextStyle(value = MaterialTheme.typography.h6) {
                        ProvideContentColor(color = ExtendedTheme.colors.onTopBar) {
                            Text(text = title, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = ExtendedTheme.colors.topBar)
        ) {
            content()
        }
    }
}

@Composable
fun Toolbar(
    title: String,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (ColumnScope.() -> Unit)? = null
) {
    TopAppBarContainer(
        topBar = {
            TopAppBar(
                title = {
                    ProvideContentColor(color = ExtendedTheme.colors.onTopBar) {
                        Text(text = title, fontWeight = FontWeight.Bold)
                    }
                },
                actions = actions,
                navigationIcon = navigationIcon,
                backgroundColor = ExtendedTheme.colors.topBar,
                contentColor = ExtendedTheme.colors.onTopBar,
                elevation = 0.dp
            )
        },
        content = content
    )
}


@Composable
fun TopAppBarContainer(
    topBar: @Composable ColumnScope.() -> Unit,
    content: @Composable (ColumnScope.() -> Unit)? = null
) {
    Column {
        Spacer(
            modifier = Modifier
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .fillMaxWidth()
                .background(color = ExtendedTheme.colors.topBar.calcStatusBarColor())
        )
        topBar()
        if (content != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = ExtendedTheme.colors.topBar),
            ) {
                content()
            }
        }
    }
}