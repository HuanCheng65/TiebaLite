package com.huanchengfly.tieba.post.ui.page.search.thread

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentColor
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
import com.huanchengfly.tieba.post.activities.UserActivity
import com.huanchengfly.tieba.post.api.models.SearchThreadBean
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.onGlobalEvent
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.compose.pullRefreshIndicator
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.ForumPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ThreadPageDestination
import com.huanchengfly.tieba.post.ui.page.search.SearchUiEvent
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.Card
import com.huanchengfly.tieba.post.ui.widgets.compose.ErrorScreen
import com.huanchengfly.tieba.post.ui.widgets.compose.ForumInfoChip
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.LoadMoreLayout
import com.huanchengfly.tieba.post.ui.widgets.compose.LocalShouldLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.MyLazyColumn
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.ui.widgets.compose.ThreadAgreeBtn
import com.huanchengfly.tieba.post.ui.widgets.compose.ThreadContent
import com.huanchengfly.tieba.post.ui.widgets.compose.ThreadReplyBtn
import com.huanchengfly.tieba.post.ui.widgets.compose.ThreadShareBtn
import com.huanchengfly.tieba.post.ui.widgets.compose.UserHeader
import com.huanchengfly.tieba.post.ui.widgets.compose.states.StateScreen
import com.huanchengfly.tieba.post.utils.DateTimeUtils
import com.huanchengfly.tieba.post.utils.StringUtil
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SearchThreadPage(
    keyword: String,
    initialSortType: Int = SearchThreadSortType.SORT_TYPE_NEWEST,
    viewModel: SearchThreadViewModel = pageViewModel(),
) {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(SearchThreadUiIntent.Refresh(keyword, initialSortType))
        viewModel.initialized = true
    }
    val currentKeyword by viewModel.uiState.collectPartialAsState(
        prop1 = SearchThreadUiState::keyword,
        initial = ""
    )
    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = SearchThreadUiState::isRefreshing,
        initial = true
    )
    val isLoadingMore by viewModel.uiState.collectPartialAsState(
        prop1 = SearchThreadUiState::isLoadingMore,
        initial = false
    )
    val error by viewModel.uiState.collectPartialAsState(
        prop1 = SearchThreadUiState::error,
        initial = null
    )
    val data by viewModel.uiState.collectPartialAsState(
        prop1 = SearchThreadUiState::data,
        initial = persistentListOf()
    )
    val currentPage by viewModel.uiState.collectPartialAsState(
        prop1 = SearchThreadUiState::currentPage,
        initial = 1
    )
    val hasMore by viewModel.uiState.collectPartialAsState(
        prop1 = SearchThreadUiState::hasMore,
        initial = true
    )
    val sortType by viewModel.uiState.collectPartialAsState(
        prop1 = SearchThreadUiState::sortType,
        initial = initialSortType
    )

    onGlobalEvent<SearchThreadUiEvent.SwitchSortType> {
        viewModel.send(SearchThreadUiIntent.Refresh(keyword, it.sortType))
    }
    val shouldLoad = LocalShouldLoad.current
    LaunchedEffect(currentKeyword) {
        if (currentKeyword.isNotEmpty() && keyword != currentKeyword) {
            if (shouldLoad) {
                viewModel.send(SearchThreadUiIntent.Refresh(keyword, sortType))
            } else {
                viewModel.initialized = false
            }
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.send(SearchThreadUiIntent.Refresh(keyword, sortType)) }
    )
    val lazyListState = rememberLazyListState()

    val isEmpty by remember {
        derivedStateOf { data.isEmpty() }
    }

    onGlobalEvent<SearchUiEvent.KeywordChanged> {
        viewModel.send(SearchThreadUiIntent.Refresh(it.keyword, sortType))
    }

    StateScreen(
        isEmpty = isEmpty,
        isError = error != null,
        isLoading = isRefreshing,
        onReload = { viewModel.send(SearchThreadUiIntent.Refresh(keyword, sortType)) },
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
            LoadMoreLayout(
                isLoading = isLoadingMore,
                onLoadMore = {
                    viewModel.send(
                        SearchThreadUiIntent.LoadMore(keyword, currentPage, sortType)
                    )
                },
                loadEnd = !hasMore,
                lazyListState = lazyListState,
            ) {
                SearchThreadList(
                    data = data,
                    lazyListState = lazyListState,
                    onItemClick = {
                        navigator.navigate(
                            ThreadPageDestination(
                                threadId = it.tid.toLong()
                            )
                        )
                    },
                    onItemUserClick = {
                        UserActivity.launch(context, it.userId)
                    },
                    onItemForumClick = {
                        navigator.navigate(
                            ForumPageDestination(
                                it.forumName
                            )
                        )
                    },
                )

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
}

@Composable
private fun SearchThreadList(
    data: ImmutableList<SearchThreadBean.ThreadInfoBean>,
    lazyListState: LazyListState,
    onItemClick: (SearchThreadBean.ThreadInfoBean) -> Unit,
    onItemUserClick: (SearchThreadBean.UserInfoBean) -> Unit,
    onItemForumClick: (SearchThreadBean.ForumInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    MyLazyColumn(
        state = lazyListState,
        modifier = modifier
    ) {
        items(data) {
            SearchThreadItem(
                item = it,
                onClick = onItemClick,
                onUserClick = onItemUserClick,
                onForumClick = onItemForumClick,
            )
        }
    }
}

@Composable
private fun SearchThreadUserHeader(
    user: SearchThreadBean.UserInfoBean,
    time: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    UserHeader(
        avatar = {
            Avatar(
                data = StringUtil.getAvatarUrl(user.portrait),
                size = Sizes.Small,
                contentDescription = null
            )
        },
        name = {
            Text(
                text = StringUtil.getUsernameAnnotatedString(
                    LocalContext.current,
                    user.userName.orEmpty(),
                    user.showNickname,
                    color = LocalContentColor.current
                )
            )
        },
        desc = {
            Text(
                text = DateTimeUtils.getRelativeTimeString(LocalContext.current, time)
            )
        },
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun SearchThreadItem(
    item: SearchThreadBean.ThreadInfoBean,
    onClick: (SearchThreadBean.ThreadInfoBean) -> Unit,
    onUserClick: (SearchThreadBean.UserInfoBean) -> Unit,
    onForumClick: (SearchThreadBean.ForumInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        header = {
            SearchThreadUserHeader(
                user = item.user,
                time = item.time,
                onClick = { onUserClick(item.user) }
            )
        },
        content = {
            ThreadContent(
                title = item.title,
                abstractText = item.content,
                showTitle = item.title.isNotBlank(),
                showAbstract = item.content.isNotBlank(),
            )
            if (item.forumName.isNotEmpty()) {
                ForumInfoChip(
                    imageUriProvider = { item.forumInfo.avatar },
                    nameProvider = { item.forumName }
                ) {
                    onForumClick(item.forumInfo)
                }
            }
        },
        action = {
            Row(modifier = Modifier.fillMaxWidth()) {
                ThreadReplyBtn(
                    replyNum = item.postNum.toInt(),
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )

                ThreadAgreeBtn(
                    hasAgree = false,
                    agreeNum = item.likeNum.toInt(),
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )

                ThreadShareBtn(
                    shareNum = item.shareNum.toLong(),
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
        },
        onClick = { onClick(item) },
    )
}