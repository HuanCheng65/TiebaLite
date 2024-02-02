package com.huanchengfly.tieba.post.ui.page.search.user

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.SearchUserBean
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.onGlobalEvent
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.compose.pullRefreshIndicator
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.UserProfilePageDestination
import com.huanchengfly.tieba.post.ui.page.search.SearchUiEvent
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.Chip
import com.huanchengfly.tieba.post.ui.widgets.compose.ErrorScreen
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.LocalShouldLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.MyLazyColumn
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.ui.widgets.compose.states.StateScreen
import com.huanchengfly.tieba.post.utils.StringUtil
import kotlinx.collections.immutable.persistentListOf


@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun SearchUserPage(
    keyword: String,
    viewModel: SearchUserViewModel = pageViewModel(),
) {
    val navigator = LocalNavigator.current
    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(SearchUserUiIntent.Refresh(keyword))
        viewModel.initialized = true
    }
    val currentKeyword by viewModel.uiState.collectPartialAsState(
        prop1 = SearchUserUiState::keyword,
        initial = ""
    )
    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = SearchUserUiState::isRefreshing,
        initial = true
    )
    val error by viewModel.uiState.collectPartialAsState(
        prop1 = SearchUserUiState::error,
        initial = null
    )
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.send(SearchUserUiIntent.Refresh(keyword)) }
    )
    val exactMatch by viewModel.uiState.collectPartialAsState(
        prop1 = SearchUserUiState::exactMatch,
        initial = null
    )
    val fuzzyMatch by viewModel.uiState.collectPartialAsState(
        prop1 = SearchUserUiState::fuzzyMatch,
        initial = persistentListOf()
    )

    val showExactMatchResult by remember {
        derivedStateOf { exactMatch != null }
    }
    val showFuzzyMatchResult by remember {
        derivedStateOf { fuzzyMatch.isNotEmpty() }
    }

    onGlobalEvent<SearchUiEvent.KeywordChanged> {
        viewModel.send(SearchUserUiIntent.Refresh(it.keyword))
    }
    val shouldLoad = LocalShouldLoad.current
    LaunchedEffect(currentKeyword) {
        if (currentKeyword.isNotEmpty() && keyword != currentKeyword) {
            if (shouldLoad) {
                viewModel.send(SearchUserUiIntent.Refresh(keyword))
            } else {
                viewModel.initialized = false
            }
        }
    }

    val isEmpty by remember {
        derivedStateOf { !showExactMatchResult && !showFuzzyMatchResult }
    }

    StateScreen(
        modifier = Modifier.fillMaxSize(),
        isEmpty = isEmpty,
        isError = error != null,
        isLoading = isRefreshing,
        onReload = { viewModel.send(SearchUserUiIntent.Refresh(keyword)) },
        errorScreen = {
            error?.let {
                val (e) = it
                ErrorScreen(error = e)
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            MyLazyColumn(modifier = Modifier.fillMaxSize()) {
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
                    exactMatch?.let {
                        item(key = "ExactMatch") {
                            SearchUserItem(
                                item = it,
                                onClick = {
                                    val id = it.id?.toLongOrNull()
                                    if (id != null) {
                                        navigator.navigate(UserProfilePageDestination(id))
                                    }
                                }
                            )
                        }
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
                                text = stringResource(id = R.string.title_fuzzy_match_user),
                                invertColor = false
                            )
                        }
                    }
                    items(fuzzyMatch) {
                        SearchUserItem(
                            item = it,
                            onClick = {
                                val id = it.id?.toLongOrNull()
                                if (id != null) {
                                    navigator.navigate(UserProfilePageDestination(id))
                                }
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
}

@Composable
private fun SearchUserItem(
    item: SearchUserBean.UserBean,
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
            data = StringUtil.getAvatarUrl(item.portrait),
            size = Sizes.Medium,
            contentDescription = item.name
        )
        Column(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = StringUtil.getUsernameAnnotatedString(
                    LocalContext.current,
                    item.name.orEmpty(),
                    item.showNickname
                ),
                style = MaterialTheme.typography.subtitle1
            )
            if (!item.intro.isNullOrEmpty()) {
                Text(
                    text = item.intro,
                    style = MaterialTheme.typography.body2,
                    maxLines = 1
                )
            }
        }
    }
}