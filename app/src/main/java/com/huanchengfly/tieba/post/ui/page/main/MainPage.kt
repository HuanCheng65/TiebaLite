package com.huanchengfly.tieba.post.ui.page.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.huanchengfly.tieba.post.MainActivityV2
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.windowsizeclass.WindowHeightSizeClass
import com.huanchengfly.tieba.post.ui.common.windowsizeclass.WindowWidthSizeClass
import com.huanchengfly.tieba.post.ui.common.windowsizeclass.calculateWindowSizeClass
import com.huanchengfly.tieba.post.ui.page.ProvideNavigator
import com.huanchengfly.tieba.post.ui.page.main.explore.ExplorePage
import com.huanchengfly.tieba.post.ui.page.main.home.HomePage
import com.huanchengfly.tieba.post.ui.page.main.notifications.NotificationsPage
import com.huanchengfly.tieba.post.ui.page.main.user.UserPage
import com.huanchengfly.tieba.post.ui.utils.*
import com.huanchengfly.tieba.post.utils.appPreferences
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Composable
private fun NavigationWrapper(
    currentPosition: Int,
    onChangePosition: (position: Int) -> Unit,
    navigationItems: List<NavigationItem>,
    navigationType: MainNavigationType,
    navigationContentPosition: MainNavigationContentPosition,
    content: @Composable () -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = navigationType == MainNavigationType.PERMANENT_NAVIGATION_DRAWER) {
            NavigationDrawerContent(
                currentPosition = currentPosition,
                onChangePosition = onChangePosition,
                navigationItems = navigationItems,
                navigationContentPosition = navigationContentPosition
            )
        }
        AnimatedVisibility(visible = navigationType == MainNavigationType.NAVIGATION_RAIL) {
            NavigationRail(
                currentPosition = currentPosition,
                onChangePosition = onChangePosition,
                navigationItems = navigationItems,
                navigationContentPosition = navigationContentPosition
            )
        }
        Column(modifier = Modifier
            .weight(1f)
            .fillMaxHeight()) {
            content()
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@RootNavGraph(start = true)
@Destination
@Composable
fun MainPage(
    navigator: DestinationsNavigator,
    activity: MainActivityV2,
    viewModel: MainViewModel = pageViewModel<MainUiIntent, MainViewModel>(emptyList()),
) {
    val windowSizeClass = calculateWindowSizeClass(activity = activity)
    val devicePostureFlow = WindowInfoTracker.getOrCreate(activity).windowLayoutInfo(activity)
        .flowWithLifecycle(activity.lifecycle)
        .map { layoutInfo ->
            val foldingFeature =
                layoutInfo.displayFeatures
                    .filterIsInstance<FoldingFeature>()
                    .firstOrNull()
            when {
                isBookPosture(foldingFeature) ->
                    DevicePosture.BookPosture(foldingFeature.bounds)

                isSeparating(foldingFeature) ->
                    DevicePosture.Separating(foldingFeature.bounds, foldingFeature.orientation)

                else -> DevicePosture.NormalPosture
            }
        }
        .stateIn(
            scope = activity.lifecycleScope,
            started = SharingStarted.Eagerly,
            initialValue = DevicePosture.NormalPosture
        )
    val foldingDevicePosture by devicePostureFlow.collectAsState()
    val messageCount by viewModel.uiState.collectPartialAsState(
        prop1 = MainUiState::messageCount,
        initial = 0
    )
    val navigationItems = listOfNotNull(
        NavigationItem(
            icon = { if (it) Icons.Rounded.Inventory2 else Icons.Outlined.Inventory2 },
            title = stringResource(id = R.string.title_main),
            content = {
                HomePage()
            }
        ),
        if (LocalContext.current.appPreferences.hideExplore) null
        else NavigationItem(
            icon = {
                if (it) ImageVector.vectorResource(id = R.drawable.ic_round_toys) else ImageVector.vectorResource(
                    id = R.drawable.ic_outline_toys
                )
            },
            title = stringResource(id = R.string.title_explore),
            content = {
                ExplorePage()
            }
        ),
        NavigationItem(
            icon = { if (it) Icons.Rounded.Notifications else Icons.Outlined.Notifications },
            title = stringResource(id = R.string.title_notifications),
            badge = messageCount > 0,
            badgeText = "$messageCount",
            content = {
                NotificationsPage()
            }
        ),
        NavigationItem(
            icon = { if (it) Icons.Rounded.AccountCircle else Icons.Outlined.AccountCircle },
            title = stringResource(id = R.string.title_user),
            content = {
                UserPage()
            }
        ),
    )
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    val themeColors = ExtendedTheme.colors

    val navigationType = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            MainNavigationType.BOTTOM_NAVIGATION
        }
        WindowWidthSizeClass.Medium -> {
            MainNavigationType.NAVIGATION_RAIL
        }
        WindowWidthSizeClass.Expanded -> {
            if (foldingDevicePosture is DevicePosture.BookPosture) {
                MainNavigationType.NAVIGATION_RAIL
            } else {
                MainNavigationType.PERMANENT_NAVIGATION_DRAWER
            }
        }
        else -> {
            MainNavigationType.BOTTOM_NAVIGATION
        }
    }

    /**
     * Content inside Navigation Rail/Drawer can also be positioned at top, bottom or center for
     * ergonomics and reachability depending upon the height of the device.
     */
    val navigationContentPosition = when (windowSizeClass.heightSizeClass) {
        WindowHeightSizeClass.Compact -> {
            MainNavigationContentPosition.TOP
        }
        WindowHeightSizeClass.Medium,
        WindowHeightSizeClass.Expanded -> {
            MainNavigationContentPosition.CENTER
        }
        else -> {
            MainNavigationContentPosition.TOP
        }
    }
    NavigationWrapper(
        currentPosition = pagerState.currentPage,
        onChangePosition = { coroutineScope.launch { pagerState.scrollToPage(it) } },
        navigationItems = navigationItems,
        navigationType = navigationType,
        navigationContentPosition = navigationContentPosition
    ) {
        Scaffold(
            backgroundColor = Color.Transparent,
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                AnimatedVisibility(visible = navigationType == MainNavigationType.BOTTOM_NAVIGATION) {
                    BottomNavigation(
                        currentPosition = pagerState.currentPage,
                        onChangePosition = { coroutineScope.launch { pagerState.scrollToPage(it) } },
                        navigationItems = navigationItems,
                        themeColors = themeColors,
                    )
                }
            }
        ) { paddingValues ->
            HorizontalPager(
                contentPadding = paddingValues,
                count = navigationItems.size,
                state = pagerState,
                key = { navigationItems[it].title },
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top,
                userScrollEnabled = false
            ) {
                ProvideNavigator(navigator = navigator) {
                    navigationItems[it].content()
                }
            }
        }
    }
    BackHandler(enabled = pagerState.currentPage != 0) {
        coroutineScope.launch {
            pagerState.scrollToPage(0)
        }
    }
}

