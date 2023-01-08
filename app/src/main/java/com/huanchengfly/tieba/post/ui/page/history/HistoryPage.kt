package com.huanchengfly.tieba.post.ui.page.history

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.page.ProvideNavigator
import com.huanchengfly.tieba.post.ui.page.history.list.HistoryListPage
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.MyScaffold
import com.huanchengfly.tieba.post.ui.widgets.compose.PagerTabIndicator
import com.huanchengfly.tieba.post.ui.widgets.compose.TitleCentredToolbar
import com.huanchengfly.tieba.post.utils.HistoryUtil
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Destination(
    deepLinks = [
        DeepLink(uriPattern = "tblite://history")
    ]
)
@Composable
fun HistoryPage(
    navigator: DestinationsNavigator
) {
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    MyScaffold(
        topBar = {
            TitleCentredToolbar(
                title = stringResource(id = R.string.title_history),
                navigationIcon = {
                    BackNavigationIcon(onBackPressed = { navigator.navigateUp() })
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(id = R.string.title_history_delete),
                            tint = ExtendedTheme.colors.onTopBar
                        )
                    }
                }
            ) {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    indicator = { tabPositions ->
                        PagerTabIndicator(
                            pagerState = pagerState,
                            tabPositions = tabPositions
                        )
                    },
                    divider = {},
                    backgroundColor = ExtendedTheme.colors.topBar,
                    contentColor = ExtendedTheme.colors.accent,
                    modifier = Modifier
                        .width(100.dp * 2)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Tab(
                        text = {
                            Text(
                                text = stringResource(id = R.string.title_history_thread),
                                fontSize = 13.sp
                            )
                        },
                        selected = pagerState.currentPage == 0,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        },
                        selectedContentColor = ExtendedTheme.colors.accent,
                        unselectedContentColor = ExtendedTheme.colors.onTopBarSecondary
                    )
                    Tab(
                        text = {
                            Text(
                                text = stringResource(id = R.string.title_history_forum),
                                fontSize = 13.sp
                            )
                        },
                        selected = pagerState.currentPage == 1,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        },
                        selectedContentColor = ExtendedTheme.colors.accent,
                        unselectedContentColor = ExtendedTheme.colors.onTopBarSecondary
                    )
                }
            }
        }
    ) {
        ProvideNavigator(navigator = navigator) {
        HorizontalPager(
            count = 2,
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
            userScrollEnabled = true,
        ) {
            if (it == 0) {
                HistoryListPage(type = HistoryUtil.TYPE_THREAD)
            } else {
                HistoryListPage(type = HistoryUtil.TYPE_FORUM)
            }
            }
        }
    }
}