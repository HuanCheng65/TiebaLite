package com.huanchengfly.tieba.post.ui.page.search.forum

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.SearchForumBean
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.compose.pullRefreshIndicator
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.ForumPageDestination
import com.huanchengfly.tieba.post.ui.widgets.Chip
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.LocalShouldLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun SearchForumPage(
    keyword: String,
    viewModel: SearchForumViewModel = pageViewModel(),
) {
    val navigator = LocalNavigator.current
    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(SearchForumUiIntent.Refresh(keyword))
        viewModel.initialized = true
    }

    val shouldLoad = LocalShouldLoad.current
    LaunchedEffect(keyword) {
        if (viewModel.initialized) {
            if (shouldLoad) {
                viewModel.send(SearchForumUiIntent.Refresh(keyword))
            } else {
                viewModel.initialized = false
            }
        }
    }
    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = SearchForumUiState::isRefreshing,
        initial = false
    )
    val exactMatchForum by viewModel.uiState.collectPartialAsState(
        prop1 = SearchForumUiState::exactMatchForum,
        initial = null
    )
    val fuzzyMatchForumList by viewModel.uiState.collectPartialAsState(
        prop1 = SearchForumUiState::fuzzyMatchForumList,
        initial = persistentListOf()
    )

    val showExactMatchResult by remember {
        derivedStateOf { exactMatchForum != null }
    }
    val showFuzzyMatchResult by remember {
        derivedStateOf { fuzzyMatchForumList.isNotEmpty() }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.send(SearchForumUiIntent.Refresh(keyword)) }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (showExactMatchResult) {
                stickyHeader(key = "ExactMatchHeader") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ExtendedTheme.colors.background)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Chip(
                            text = stringResource(id = R.string.title_exact_match),
                            invertColor = true
                        )
                    }
                }
                item(key = "ExactMatch") {
                    SearchForumItem(
                        item = exactMatchForum!!,
                        onClick = {
                            navigator.navigate(ForumPageDestination(exactMatchForum!!.forumName.orEmpty()))
                        }
                    )
                }
            }
            if (showFuzzyMatchResult) {
                stickyHeader(key = "FuzzyMatchHeader") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ExtendedTheme.colors.background)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Chip(
                            text = stringResource(id = R.string.title_fuzzy_match),
                            invertColor = false
                        )
                    }
                }
                items(fuzzyMatchForumList) {
                    SearchForumItem(
                        item = it,
                        onClick = {
                            navigator.navigate(ForumPageDestination(it.forumName.orEmpty()))
                        }
                    )
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = ExtendedTheme.colors.pullRefreshIndicator,
            contentColor = ExtendedTheme.colors.primary,
        )
    }
}

@Composable
private fun SearchForumItem(
    item: SearchForumBean.ForumInfoBean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Avatar(
            data = item.avatar,
            size = Sizes.Medium,
            contentDescription = item.forumNameShow
        )
        Column(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.title_forum, item.forumNameShow.orEmpty()),
                style = MaterialTheme.typography.subtitle1
            )
            if (!item.intro.isNullOrEmpty()) {
                Text(
                    text = item.slogan.orEmpty(),
                    style = MaterialTheme.typography.body2,
                    maxLines = 1
                )
            }
        }
    }
}