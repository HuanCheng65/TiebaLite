package com.huanchengfly.tieba.post.ui.page.main.explore

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.Scaffold
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.NewSearchActivity
import com.huanchengfly.tieba.post.arch.onEvent
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.page.main.MainUiEvent
import com.huanchengfly.tieba.post.ui.page.main.explore.concern.ConcernPage
import com.huanchengfly.tieba.post.ui.page.main.explore.hot.HotPage
import com.huanchengfly.tieba.post.ui.page.main.explore.personalized.PersonalizedPage
import com.huanchengfly.tieba.post.ui.widgets.compose.ActionItem
import com.huanchengfly.tieba.post.ui.widgets.compose.PagerTabIndicator
import com.huanchengfly.tieba.post.ui.widgets.compose.Toolbar
import com.huanchengfly.tieba.post.ui.widgets.compose.accountNavIconIfCompact
import com.huanchengfly.tieba.post.utils.AccountUtil.LocalAccount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch


@OptIn(ExperimentalPagerApi::class)
@Composable
fun ExplorePage(
    eventFlow: Flow<MainUiEvent>,
) {
    val account = LocalAccount.current
    val context = LocalContext.current

    val eventFlows = remember {
        listOf(
            MutableSharedFlow<MainUiEvent>(),
            MutableSharedFlow<MainUiEvent>(),
            MutableSharedFlow<MainUiEvent>()
        )
    }

    val firstIndex = if (account != null) 0 else -1

    val pages = listOfNotNull<Pair<String, (@Composable () -> Unit)>>(
        if (account != null) stringResource(id = R.string.title_concern) to @Composable {
            ConcernPage(eventFlows[firstIndex + 0])
        } else null,
        stringResource(id = R.string.title_personalized) to @Composable {
            PersonalizedPage(eventFlows[firstIndex + 1])
        },
        stringResource(id = R.string.title_hot) to @Composable {
            HotPage(eventFlows[firstIndex + 2])
        },
    )
    val pagerState = rememberPagerState(initialPage = if (account != null) 1 else 0)
    val coroutineScope = rememberCoroutineScope()

    eventFlow.onEvent<MainUiEvent.Refresh> {
        eventFlows[pagerState.currentPage].emit(it)
    }

    Scaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            Toolbar(
                title = stringResource(id = R.string.title_explore),
                navigationIcon = accountNavIconIfCompact(),
                actions = {
                    ActionItem(
                        icon = Icons.Rounded.Search,
                        contentDescription = stringResource(id = R.string.title_search)
                    ) {
                        context.goToActivity<NewSearchActivity>()
                    }
                },
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
                    backgroundColor = Color.Transparent,
                    contentColor = ExtendedTheme.colors.onTopBar,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(76.dp * pages.size),
                ) {
                    pages.forEachIndexed { index, pair ->
                        Tab(
                            text = { Text(text = pair.first) },
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    if (pagerState.currentPage == index) {
                                        eventFlows[pagerState.currentPage].emit(MainUiEvent.Refresh)
                                    } else {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                            },
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        HorizontalPager(
            contentPadding = paddingValues,
            count = pages.size,
            state = pagerState,
            key = { pages[it].first },
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
            userScrollEnabled = true,
        ) {
            pages[it].second()
        }
    }
}