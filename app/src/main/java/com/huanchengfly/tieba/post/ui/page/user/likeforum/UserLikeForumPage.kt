package com.huanchengfly.tieba.post.ui.page.user.likeforum

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.api.models.UserLikeForumBean
import com.huanchengfly.tieba.post.arch.GlobalEvent
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.getOrNull
import com.huanchengfly.tieba.post.arch.onGlobalEvent
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.compose.pullRefreshIndicator
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.ForumPageDestination
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.ErrorScreen
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.LoadMoreLayout
import com.huanchengfly.tieba.post.ui.widgets.compose.MyLazyColumn
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.ui.widgets.compose.states.StateScreen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UserLikeForumPage(
    uid: Long,
    viewModel: UserLikeForumViewModel = pageViewModel(),
) {
    val navigator = LocalNavigator.current

    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(UserLikeForumUiIntent.Refresh(uid))
        viewModel.initialized = true
    }

    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = UserLikeForumUiState::isRefreshing,
        initial = true
    )
    val isLoadingMore by viewModel.uiState.collectPartialAsState(
        prop1 = UserLikeForumUiState::isLoadingMore,
        initial = false
    )
    val error by viewModel.uiState.collectPartialAsState(
        prop1 = UserLikeForumUiState::error,
        initial = null
    )
    val currentPage by viewModel.uiState.collectPartialAsState(
        prop1 = UserLikeForumUiState::currentPage,
        initial = 1
    )
    val hasMore by viewModel.uiState.collectPartialAsState(
        prop1 = UserLikeForumUiState::hasMore,
        initial = false
    )
    val forums by viewModel.uiState.collectPartialAsState(
        prop1 = UserLikeForumUiState::forums,
        initial = persistentListOf()
    )

    val isEmpty by remember {
        derivedStateOf { forums.isEmpty() }
    }
    val isError by remember {
        derivedStateOf { error != null }
    }

    onGlobalEvent<GlobalEvent.Refresh>(
        filter = { it.key == "user_profile" }
    ) {
        viewModel.send(UserLikeForumUiIntent.Refresh(uid))
    }

    StateScreen(
        isEmpty = isEmpty,
        isError = isError,
        isLoading = isRefreshing,
        onReload = {
            viewModel.send(UserLikeForumUiIntent.Refresh(uid))
        },
        errorScreen = { ErrorScreen(error = error.getOrNull()) },
    ) {
        val pullRefreshState = rememberPullRefreshState(
            refreshing = isRefreshing,
            onRefresh = ::reload
        )

        val lazyListState = rememberLazyListState()

        Box {
            LoadMoreLayout(
                isLoading = isLoadingMore,
                onLoadMore = {
                    viewModel.send(UserLikeForumUiIntent.LoadMore(uid, currentPage))
                },
                loadEnd = !hasMore,
                lazyListState = lazyListState
            ) {
                UserLikeForumList(
                    forums = { forums },
                    onClickForum = { forumBean ->
                        forumBean.name?.let {
                            navigator.navigate(ForumPageDestination(it))
                        }
                    },
                    lazyListState = lazyListState
                )
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
private fun UserLikeForumList(
    forums: () -> ImmutableList<UserLikeForumBean.ForumBean>,
    onClickForum: (UserLikeForumBean.ForumBean) -> Unit,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    val data = remember(forums) { forums() }
    MyLazyColumn(state = lazyListState) {
        items(
            items = data,
            key = { it.id }
        ) {
            UserLikeForumItem(
                item = it,
                onClick = {
                    onClickForum(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun UserLikeForumItem(
    item: UserLikeForumBean.ForumBean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Avatar(
            data = item.avatar,
            size = Sizes.Medium,
            contentDescription = null
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = item.name.orEmpty(), style = MaterialTheme.typography.subtitle1)
            item.slogan.takeUnless { it.isNullOrEmpty() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.body2,
                    color = ExtendedTheme.colors.textSecondary
                )
            }
        }
    }
}