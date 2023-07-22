package com.huanchengfly.tieba.post.ui.page.main.explore

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Scaffold
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.NewSearchActivity
import com.huanchengfly.tieba.post.arch.GlobalEvent
import com.huanchengfly.tieba.post.arch.emitGlobalEvent
import com.huanchengfly.tieba.post.arch.onGlobalEvent
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.page.main.explore.concern.ConcernPage
import com.huanchengfly.tieba.post.ui.page.main.explore.hot.HotPage
import com.huanchengfly.tieba.post.ui.page.main.explore.personalized.PersonalizedPage
import com.huanchengfly.tieba.post.ui.widgets.compose.ActionItem
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoadHorizontalPager
import com.huanchengfly.tieba.post.ui.widgets.compose.PagerTabIndicator
import com.huanchengfly.tieba.post.ui.widgets.compose.Toolbar
import com.huanchengfly.tieba.post.ui.widgets.compose.accountNavIconIfCompact
import com.huanchengfly.tieba.post.utils.AccountUtil.LocalAccount
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch


@Immutable
data class ExplorePageItem(
    val id: String,
    val name: @Composable () -> Unit,
    val content: @Composable () -> Unit,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ColumnScope.ExplorePageTab(
    pagerState: PagerState,
    pages: ImmutableList<ExplorePageItem>
) {
    val coroutineScope = rememberCoroutineScope()

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
        pages.forEachIndexed { index, item ->
            Tab(
                text = item.name,
                selected = pagerState.currentPage == index,
                onClick = {
                    coroutineScope.launch {
                        if (pagerState.currentPage == index) {
                            coroutineScope.emitGlobalEvent(GlobalEvent.Refresh(item.id))
                        } else {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExplorePage() {
    val account = LocalAccount.current
    val context = LocalContext.current

    val loggedIn = remember(account) { account != null }

    val pages = remember {
        listOfNotNull(
            if (loggedIn) ExplorePageItem(
                "concern",
                { Text(text = stringResource(id = R.string.title_concern)) },
                { ConcernPage() }
            ) else null,
            ExplorePageItem(
                "personalized",
                { Text(text = stringResource(id = R.string.title_personalized)) },
                { PersonalizedPage() }
            ),
            ExplorePageItem(
                "hot",
                { Text(text = stringResource(id = R.string.title_hot)) },
                { HotPage() }
            ),
        ).toImmutableList()
    }
    val pagerState = rememberPagerState(initialPage = if (account != null) 1 else 0)
    val coroutineScope = rememberCoroutineScope()

    onGlobalEvent<GlobalEvent.Refresh>(
        filter = { it.key == "explore" }
    ) {
        coroutineScope.emitGlobalEvent(GlobalEvent.Refresh(pages[pagerState.currentPage].id))
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
                ExplorePageTab(pagerState = pagerState, pages = pages)
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        LazyLoadHorizontalPager(
            contentPadding = paddingValues,
            pageCount = pages.size,
            state = pagerState,
            key = { pages[it].id },
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
            userScrollEnabled = true,
        ) {
            pages[it].content()
        }
    }
}