package com.huanchengfly.tieba.post.ui.page.user.post

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.protos.OriginThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.PostInfoList
import com.huanchengfly.tieba.post.arch.GlobalEvent
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.getOrNull
import com.huanchengfly.tieba.post.arch.onGlobalEvent
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.compose.pullRefreshIndicator
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.ForumPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ThreadPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.UserProfilePageDestination
import com.huanchengfly.tieba.post.ui.widgets.compose.Button
import com.huanchengfly.tieba.post.ui.widgets.compose.ErrorScreen
import com.huanchengfly.tieba.post.ui.widgets.compose.FeedCard
import com.huanchengfly.tieba.post.ui.widgets.compose.FeedCardPlaceholder
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.LoadMoreLayout
import com.huanchengfly.tieba.post.ui.widgets.compose.MyLazyColumn
import com.huanchengfly.tieba.post.ui.widgets.compose.TipScreen
import com.huanchengfly.tieba.post.ui.widgets.compose.states.StateScreen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UserPostPage(
    uid: Long,
    isThread: Boolean = true,
    viewModel: UserPostViewModel = pageViewModel(key = if (isThread) "user_thread_$uid" else "user_post_$uid"),
) {
    val navigator = LocalNavigator.current

    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(UserPostUiIntent.Refresh(uid, isThread))
        viewModel.initialized = true
    }

    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = UserPostUiState::isRefreshing,
        initial = true
    )
    val isLoadingMore by viewModel.uiState.collectPartialAsState(
        prop1 = UserPostUiState::isLoadingMore,
        initial = false
    )
    val error by viewModel.uiState.collectPartialAsState(
        prop1 = UserPostUiState::error,
        initial = null
    )
    val currentPage by viewModel.uiState.collectPartialAsState(
        prop1 = UserPostUiState::currentPage,
        initial = 1
    )
    val hasMore by viewModel.uiState.collectPartialAsState(
        prop1 = UserPostUiState::hasMore,
        initial = false
    )
    val posts by viewModel.uiState.collectPartialAsState(
        prop1 = UserPostUiState::posts,
        initial = persistentListOf()
    )
    val hidePost by viewModel.uiState.collectPartialAsState(
        prop1 = UserPostUiState::hidePost,
        initial = false
    )

    val isEmpty by remember {
        derivedStateOf { posts.isEmpty() }
    }
    val isError by remember {
        derivedStateOf { error != null }
    }

    onGlobalEvent<GlobalEvent.Refresh>(
        filter = { it.key == "user_profile" }
    ) {
        viewModel.send(UserPostUiIntent.Refresh(uid, isThread))
    }

    StateScreen(
        modifier = Modifier.fillMaxSize(),
        isEmpty = isEmpty,
        isError = isError,
        isLoading = isRefreshing,
        onReload = {
            viewModel.send(UserPostUiIntent.Refresh(uid, isThread))
        },
        loadingScreen = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column {
                    repeat(4) {
                        FeedCardPlaceholder()
                    }
                }
            }
        },
        errorScreen = { ErrorScreen(error = error.getOrNull()) },
        emptyScreen = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                if (hidePost) {
                    TipScreen(
                        title = { Text(text = stringResource(id = R.string.title_user_hide_post)) },
                        image = {
                            val composition by rememberLottieComposition(
                                LottieCompositionSpec.RawRes(
                                    R.raw.lottie_hide
                                )
                            )
                            LottieAnimation(
                                composition = composition,
                                iterations = LottieConstants.IterateForever,
                                modifier = Modifier
                                    .padding(vertical = 16.dp)
                                    .fillMaxWidth()
                                    .aspectRatio(2.5f)
                            )
                        }
                    )
                } else {
                    TipScreen(
                        title = { Text(text = stringResource(id = R.string.title_empty)) },
                        image = {
                            val composition by rememberLottieComposition(
                                LottieCompositionSpec.RawRes(
                                    R.raw.lottie_empty_box
                                )
                            )
                            LottieAnimation(
                                composition = composition,
                                iterations = LottieConstants.IterateForever,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(2f)
                            )
                        },
                        actions = {
                            if (canReload) {
                                Button(onClick = { reload() }) {
                                    Text(text = stringResource(id = R.string.btn_refresh))
                                }
                            }
                        }
                    )
                }
            }
        },
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
                    viewModel.send(UserPostUiIntent.LoadMore(uid, isThread, currentPage))
                },
                loadEnd = !hasMore,
                lazyListState = lazyListState
            ) {
                UserPostList(
                    posts = { posts },
                    lazyListState = lazyListState,
                    onClickItem = {
                        navigator.navigate(
                            ThreadPageDestination(
                                it.thread_id,
                                it.forum_id,
                            )
                        )
                    },
                    onAgreeItem = {},
                    onClickReply = {
                        navigator.navigate(
                            ThreadPageDestination(
                                it.thread_id,
                                it.forum_id,
                                scrollToReply = true
                            )
                        )
                    },
                    onClickUser = {
                        navigator.navigate(UserProfilePageDestination(it))
                    },
                    onClickForum = {
                        navigator.navigate(ForumPageDestination(it))
                    },
                    onClickOriginThread = {
                        navigator.navigate(
                            ThreadPageDestination(
                                it.tid.toLong(),
                                it.fid,
                            )
                        )
                    },
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
private fun UserPostList(
    posts: () -> ImmutableList<ImmutableHolder<PostInfoList>>,
    lazyListState: LazyListState = rememberLazyListState(),
    onClickItem: (PostInfoList) -> Unit = {},
    onAgreeItem: (PostInfoList) -> Unit = {},
    onClickReply: (PostInfoList) -> Unit = {},
    onClickUser: (id: Long) -> Unit = {},
    onClickForum: (name: String) -> Unit = {},
    onClickOriginThread: (OriginThreadInfo) -> Unit = {},
) {
    val data = remember(posts) { posts() }
    MyLazyColumn(state = lazyListState) {
        items(
            items = data,
            key = {
                "${it.get { thread_id }}_${it.get { post_id }}"
            }
        ) {
            UserPostItem(
                post = it,
                onClick = onClickItem,
                onAgree = onAgreeItem,
                onClickReply = onClickReply,
                onClickUser = onClickUser,
                onClickForum = onClickForum,
                onClickOriginThread = onClickOriginThread,
            )
        }
    }
}

@Composable
fun UserPostItem(
    post: ImmutableHolder<PostInfoList>,
    onClick: (PostInfoList) -> Unit,
    onAgree: (PostInfoList) -> Unit,
    modifier: Modifier = Modifier,
    onClickReply: (PostInfoList) -> Unit = {},
    onClickUser: (id: Long) -> Unit = {},
    onClickForum: (name: String) -> Unit = {},
    onClickOriginThread: (OriginThreadInfo) -> Unit = {},
) {
    FeedCard(
        item = post,
        onClick = onClick,
        onAgree = onAgree,
        modifier = modifier,
        onClickReply = onClickReply,
        onClickUser = onClickUser,
        onClickForum = onClickForum,
        onClickOriginThread = onClickOriginThread,
    )
}