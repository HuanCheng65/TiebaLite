package com.huanchengfly.tieba.post.ui.page.main

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.NavigationRail
import androidx.compose.material.NavigationRailItem
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedColors
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.compose.White
import com.huanchengfly.tieba.post.ui.utils.MainNavigationContentPosition
import com.huanchengfly.tieba.post.ui.widgets.compose.AccountNavIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.utils.AccountUtil.LocalAccount

enum class LayoutType {
    HEADER, CONTENT
}

@Composable
fun PermanentNavigationDrawer(
    drawerContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(modifier.fillMaxSize()) {
        drawerContent()
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

private val ActiveIndicatorHeight = 56.dp
private val ActiveIndicatorWidth = 240.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NavigationDrawerItem(
    label: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    badge: (@Composable () -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = Color.Transparent,
    selectedBackgroundColor: Color = MaterialTheme.colors.primary.copy(0.25f),
    itemColor: Color = MaterialTheme.colors.onSurface,
    selectedItemColor: Color = MaterialTheme.colors.primary,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier
            .height(ActiveIndicatorHeight)
            .fillMaxWidth(),
        shape = shape,
        color = if (selected) selectedBackgroundColor else backgroundColor,
        interactionSource = interactionSource,
    ) {
        Row(
            Modifier.padding(start = 16.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                val iconColor = if (selected) selectedItemColor else itemColor
                CompositionLocalProvider(LocalContentColor provides iconColor, content = icon)
                Spacer(Modifier.width(12.dp))
            }
            Box(Modifier.weight(1f)) {
                val labelColor = if (selected) selectedItemColor else itemColor
                CompositionLocalProvider(LocalContentColor provides labelColor, content = label)
            }
            if (badge != null) {
                Spacer(Modifier.width(12.dp))
                val badgeColor = if (selected) selectedItemColor else itemColor
                CompositionLocalProvider(LocalContentColor provides badgeColor, content = badge)
            }
        }
    }
}

@Composable
fun NavigationDrawerContent(
    currentPosition: Int,
    onChangePosition: (position: Int) -> Unit,
    onReselected: (position: Int) -> Unit,
    navigationItems: List<NavigationItem>,
    navigationContentPosition: MainNavigationContentPosition
) {
    PositionLayout(
        modifier = Modifier
            .width(ActiveIndicatorWidth)
            .background(ExtendedTheme.colors.bottomBar)
            .padding(16.dp),
        content = {
            Column(
                modifier = Modifier
                    .layoutId(LayoutType.HEADER)
                    .statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp) // NavigationRailVerticalPadding
            ) {
                val account = LocalAccount.current
                if (account != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        AccountNavIcon(spacer = false, size = Sizes.Large)
                        Text(
                            text = account.nameShow ?: account.name,
                            style = MaterialTheme.typography.subtitle1,
                            color = ExtendedTheme.colors.text
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Avatar(
                            data = R.drawable.ic_launcher_new_round,
                            size = Sizes.Small,
                            contentDescription = stringResource(id = R.string.app_name)
                        )
                        Text(
                            text = stringResource(id = R.string.app_name).uppercase(),
                            style = MaterialTheme.typography.h6,
                            color = ExtendedTheme.colors.accent
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .layoutId(LayoutType.CONTENT)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                navigationItems.forEachIndexed { index, navigationItem ->
                    NavigationDrawerItem(
                        selected = index == currentPosition,
                        onClick = {
                            if (index == currentPosition) {
                                onReselected(index)
                            } else {
                                onChangePosition(index)
                            }
                        },
                        label = { Text(text = navigationItem.title) },
                        icon = {
                            Box {
                                Icon(
                                    imageVector = navigationItem.icon(index == currentPosition),
                                    contentDescription = navigationItem.title
                                )
                                if (navigationItem.badge) {
                                    Text(
                                        textAlign = TextAlign.Center,
                                        fontSize = 10.sp,
                                        color = White,
                                        text = navigationItem.badgeText ?: "",
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .align(Alignment.TopEnd)
                                            .background(
                                                color = MaterialTheme.colors.secondary,
                                                shape = CircleShape
                                            ),
                                    )
                                }
                            }
                        }
                    )
                }
            }
        },
        navigationContentPosition = navigationContentPosition
    )
}

@Composable
private fun PositionLayout(
    navigationContentPosition: MainNavigationContentPosition,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = content,
        measurePolicy = { measurables, constraints ->
            lateinit var headerMeasurable: Measurable
            lateinit var contentMeasurable: Measurable
            measurables.forEach {
                when (it.layoutId) {
                    LayoutType.HEADER -> headerMeasurable = it
                    LayoutType.CONTENT -> contentMeasurable = it
                    else -> error("Unknown layoutId encountered!")
                }
            }

            val headerPlaceable = headerMeasurable.measure(constraints)
            val contentPlaceable = contentMeasurable.measure(
                constraints.offset(vertical = -headerPlaceable.height)
            )
            layout(constraints.maxWidth, constraints.maxHeight) {
                // Place the header, this goes at the top
                headerPlaceable.placeRelative(0, 0)

                // Determine how much space is not taken up by the content
                val nonContentVerticalSpace = constraints.maxHeight - contentPlaceable.height

                val contentPlaceableY = when (navigationContentPosition) {
                    // Figure out the place we want to place the content, with respect to the
                    // parent (ignoring the header for now)
                    MainNavigationContentPosition.TOP -> 0
                    MainNavigationContentPosition.CENTER -> nonContentVerticalSpace / 2
                }
                    // And finally, make sure we don't overlap with the header.
                    .coerceAtLeast(headerPlaceable.height)

                contentPlaceable.placeRelative(0, contentPlaceableY)
            }
        }
    )
}

@Composable
fun NavigationRail(
    currentPosition: Int,
    onChangePosition: (position: Int) -> Unit,
    onReselected: (position: Int) -> Unit,
    navigationItems: List<NavigationItem>,
    navigationContentPosition: MainNavigationContentPosition
) {
    NavigationRail(
        backgroundColor = ExtendedTheme.colors.bottomBar,
        contentColor = ExtendedTheme.colors.unselected,
        modifier = Modifier
            .fillMaxHeight()
            .statusBarsPadding(),
        elevation = 0.dp,
        header = {
            AccountNavIcon(spacer = false)
        }
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = if (navigationContentPosition == MainNavigationContentPosition.TOP) Arrangement.Top else Arrangement.Center
        ) {
            navigationItems.forEachIndexed { index, navigationItem ->
                NavigationRailItem(
                    selected = index == currentPosition,
                    onClick = {
                        if (index == currentPosition) {
                            onReselected(index)
                        } else {
                            onChangePosition(index)
                        }
                    },
                    icon = {
                        Box {
                            Icon(
                                imageVector = navigationItem.icon(index == currentPosition),
                                contentDescription = navigationItem.title
                            )
                            if (navigationItem.badge) {
                                Text(
                                    textAlign = TextAlign.Center,
                                    fontSize = 10.sp,
                                    color = White,
                                    text = navigationItem.badgeText ?: "",
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .align(Alignment.TopEnd)
                                        .background(
                                            color = MaterialTheme.colors.secondary,
                                            shape = CircleShape
                                        ),
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationDivider(
    themeColors: ExtendedColors = ExtendedTheme.colors
) {
    if (!themeColors.isNightMode) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(themeColors.divider)
        )
    }
}

@Composable
fun BottomNavigation(
    currentPosition: Int,
    onChangePosition: (position: Int) -> Unit,
    onReselected: (position: Int) -> Unit,
    navigationItems: List<NavigationItem>,
    themeColors: ExtendedColors = ExtendedTheme.colors
) {
    Column(modifier = Modifier.navigationBarsPadding()) {
        BottomNavigationDivider(themeColors)
        BottomNavigation(
            backgroundColor = themeColors.bottomBar,
            elevation = 0.dp,
        ) {
            navigationItems.forEachIndexed { index, navigationItem ->
                BottomNavigationItem(
                    selected = index == currentPosition,
                    onClick = {
                        if (index == currentPosition) {
                            onReselected(index)
                        } else {
                            onChangePosition(index)
                        }
                        navigationItem.onClick?.invoke()
                    },
                    icon = {
                        Box {
                            Icon(
                                imageVector = navigationItem.icon(index == currentPosition),
                                contentDescription = navigationItem.title
                            )
                            if (navigationItem.badge) {
                                Text(
                                    textAlign = TextAlign.Center,
                                    fontSize = 10.sp,
                                    color = White,
                                    text = navigationItem.badgeText ?: "",
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .align(Alignment.TopEnd)
                                        .background(
                                            color = MaterialTheme.colors.secondary,
                                            shape = CircleShape
                                        ),
                                )
                            }
                        }
                    },
                    selectedContentColor = MaterialTheme.colors.secondary,
                    unselectedContentColor = themeColors.unselected,
                    alwaysShowLabel = false
                )
            }
        }
    }
}

data class NavigationItem(
    val icon: @Composable (selected: Boolean) -> ImageVector,
    val title: String,
    val badge: Boolean = false,
    val badgeText: String? = null,
    val onClick: (() -> Unit)? = null,
    val content: @Composable () -> Unit = {}
)